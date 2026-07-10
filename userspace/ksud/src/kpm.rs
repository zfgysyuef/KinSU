use std::{
    ffi::CString,
    fs, io,
    os::unix::fs::PermissionsExt,
    path::{Path, PathBuf},
};

use anyhow::{Context, Result, bail};

use crate::ksu_uapi;
use crate::ksucalls::ksuctl;

pub const KPM_DIR: &str = "/data/adb/kpm";
const KPM_BUFFER_SIZE: usize = 4096;

fn run_kpm(control_code: u32, arg1: u64, arg2: u64) -> Result<i32> {
    let mut result = -libc::ENOSYS;
    let mut cmd = ksu_uapi::ksu_kpm_cmd {
        control_code: u64::from(control_code),
        arg1,
        arg2,
        result_code: (&raw mut result) as u64,
    };

    ksuctl(ksu_uapi::KSU_IOCTL_KPM, &raw mut cmd).context("KPM ioctl failed")?;
    Ok(result)
}

fn check_result(operation: &str, result: i32) -> Result<()> {
    if result < 0 {
        bail!(
            "{operation}: {}",
            io::Error::from_raw_os_error(result.saturating_neg())
        );
    }
    if result != 0 {
        bail!("{operation}: unexpected result {result}");
    }
    Ok(())
}

pub fn load_module<P>(path: P, args: Option<&str>) -> Result<()>
where
    P: AsRef<Path>,
{
    let path = CString::new(path.as_ref().to_string_lossy().as_bytes())?;
    let args = CString::new(args.unwrap_or_default())?;
    let result = run_kpm(
        ksu_uapi::SUKISU_KPM_LOAD,
        path.as_ptr() as u64,
        args.as_ptr() as u64,
    )?;
    check_result("failed to load KPM", result)
}

pub fn unload_module(name: &str) -> Result<()> {
    let name = CString::new(name)?;
    let result = run_kpm(ksu_uapi::SUKISU_KPM_UNLOAD, name.as_ptr() as u64, 0)?;
    check_result("failed to unload KPM", result)
}

pub fn num() -> Result<i32> {
    let result = run_kpm(ksu_uapi::SUKISU_KPM_NUM, 0, 0)?;
    if result < 0 {
        bail!(
            "failed to get KPM count: {}",
            io::Error::from_raw_os_error(result.saturating_neg())
        );
    }
    Ok(result)
}

pub fn list() -> Result<String> {
    let mut buf = vec![0u8; KPM_BUFFER_SIZE];
    let result = run_kpm(
        ksu_uapi::SUKISU_KPM_LIST,
        buf.as_mut_ptr() as u64,
        buf.len() as u64,
    )?;
    if result < 0 {
        bail!(
            "failed to list KPM modules: {}",
            io::Error::from_raw_os_error(result.saturating_neg())
        );
    }
    Ok(buf2str(&buf))
}

pub fn info(name: &str) -> Result<String> {
    let name = CString::new(name)?;
    let mut buf = vec![0u8; 256];
    let result = run_kpm(
        ksu_uapi::SUKISU_KPM_INFO,
        name.as_ptr() as u64,
        buf.as_mut_ptr() as u64,
    )?;
    check_result("failed to get KPM info", result)?;
    Ok(buf2str(&buf))
}

pub fn control(name: &str, args: Option<&str>) -> Result<i32> {
    let name = CString::new(name)?;
    let args = CString::new(args.unwrap_or_default())?;
    let result = run_kpm(
        ksu_uapi::SUKISU_KPM_CONTROL,
        name.as_ptr() as u64,
        args.as_ptr() as u64,
    )?;
    if result < 0 {
        bail!(
            "failed to control KPM: {}",
            io::Error::from_raw_os_error(result.saturating_neg())
        );
    }
    Ok(result)
}

pub fn version() -> Result<String> {
    let mut buf = vec![0u8; 1024];
    let result = run_kpm(
        ksu_uapi::SUKISU_KPM_VERSION,
        buf.as_mut_ptr() as u64,
        buf.len() as u64,
    )?;
    check_result("failed to get KernelPatch version", result)?;

    let version = buf2str(&buf).trim().to_owned();
    if version.is_empty() {
        bail!("KPM is unavailable: the KernelPatch bridge is not active");
    }
    Ok(version)
}

fn ensure_dir() -> Result<()> {
    let dir = Path::new(KPM_DIR);
    fs::create_dir_all(dir).with_context(|| format!("create {}", dir.display()))?;
    fs::set_permissions(dir, fs::Permissions::from_mode(0o700))
        .with_context(|| format!("set permissions on {}", dir.display()))?;
    Ok(())
}

pub fn booted_load() -> Result<()> {
    let dir = Path::new(KPM_DIR);
    if !dir.exists() {
        return Ok(());
    }

    let version = version()?;
    log::info!("KPM bridge active: {version}");
    ensure_dir()?;

    if crate::utils::is_safe_mode() {
        log::warn!("KPM: safe mode is active; persistent modules will not be loaded");
        return Ok(());
    }

    let mut modules: Vec<PathBuf> = fs::read_dir(dir)?
        .filter_map(|entry| entry.ok().map(|entry| entry.path()))
        .filter(|path| {
            path.is_file()
                && path
                    .extension()
                    .is_some_and(|extension| extension.eq_ignore_ascii_case("kpm"))
        })
        .collect();
    modules.sort();

    for module in modules {
        if let Err(error) = load_module(&module, None) {
            log::error!("KPM: failed to load {}: {error:#}", module.display());
        } else {
            log::info!("KPM: loaded {}", module.display());
        }
    }

    Ok(())
}

fn buf2str(buf: &[u8]) -> String {
    let end = buf.iter().position(|byte| *byte == 0).unwrap_or(buf.len());
    String::from_utf8_lossy(&buf[..end]).into_owned()
}
