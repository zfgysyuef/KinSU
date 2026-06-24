use std::ffi::CString;
use std::ptr;

use anyhow::{bail, Result};
use log::{info, error};

use crate::ksucalls::ksuctl;

// KPM ioctl command codes (must match kernel uapi/supercall.h)
// _IOWR('K', nr, sizeof(struct)) = 0xC0000000 | (size<<16) | (0x4B<<8) | nr
const KSU_IOCTL_KPM_LOAD: u32 = 0xC018_4B30;    // nr=0x30, size=24
const KSU_IOCTL_KPM_UNLOAD: u32 = 0xC010_4B31;  // nr=0x31, size=16
const KSU_IOCTL_KPM_NUMS: u32 = 0xC008_4B32;    // nr=0x32, size=8
const KSU_IOCTL_KPM_LIST: u32 = 0xC010_4B33;    // nr=0x33, size=16
const KSU_IOCTL_KPM_INFO: u32 = 0xC018_4B34;    // nr=0x34, size=24
const KSU_IOCTL_KPM_CONTROL: u32 = 0xC020_4B35; // nr=0x35, size=32

#[repr(C)]
struct KsuKpmLoadCmd {
    path: u64,
    args: u64,
    result: i32,
    reserved: i32,
}

#[repr(C)]
struct KsuKpmUnloadCmd {
    name: u64,
    result: i32,
    reserved: i32,
}

#[repr(C)]
struct KsuKpmNumsCmd {
    nums: i32,
    reserved: i32,
}

#[repr(C)]
struct KsuKpmListCmd {
    buf: u64,
    buf_size: u32,
    result: i32,
}

#[repr(C)]
struct KsuKpmInfoCmd {
    name: u64,
    buf: u64,
    buf_size: u32,
    result: i32,
}

#[repr(C)]
struct KsuKpmControlCmd {
    name: u64,
    args: u64,
    out_buf: u64,
    out_len: i32,
    result: i32,
}

pub fn kpm_load_module(path: &str, args: &str) -> std::io::Result<i32> {
    let path_c = CString::new(path).unwrap();
    let args_c = CString::new(args).unwrap();

    let mut cmd = KsuKpmLoadCmd {
        path: path_c.as_ptr() as u64,
        args: args_c.as_ptr() as u64,
        result: 0,
        reserved: 0,
    };

    let ret = ksuctl(KSU_IOCTL_KPM_LOAD, &raw mut cmd as *mut _)?;
    if ret < 0 {
        return Err(std::io::Error::last_os_error());
    }
    Ok(cmd.result)
}

pub fn kpm_unload_module(name: &str) -> std::io::Result<i32> {
    let name_c = CString::new(name).unwrap();

    let mut cmd = KsuKpmUnloadCmd {
        name: name_c.as_ptr() as u64,
        result: 0,
        reserved: 0,
    };

    let ret = ksuctl(KSU_IOCTL_KPM_UNLOAD, &raw mut cmd as *mut _)?;
    if ret < 0 {
        return Err(std::io::Error::last_os_error());
    }
    Ok(cmd.result)
}

pub fn kpm_module_nums() -> std::io::Result<i32> {
    let mut cmd = KsuKpmNumsCmd {
        nums: 0,
        reserved: 0,
    };

    let ret = ksuctl(KSU_IOCTL_KPM_NUMS, &raw mut cmd as *mut _)?;
    if ret < 0 {
        return Err(std::io::Error::last_os_error());
    }
    Ok(cmd.nums)
}

pub fn kpm_module_list(buf: &mut [u8]) -> std::io::Result<usize> {
    let mut cmd = KsuKpmListCmd {
        buf: buf.as_mut_ptr() as u64,
        buf_size: buf.len() as u32,
        result: 0,
    };

    let ret = ksuctl(KSU_IOCTL_KPM_LIST, &raw mut cmd as *mut _)?;
    if ret < 0 {
        return Err(std::io::Error::last_os_error());
    }
    if cmd.result < 0 {
        return Err(std::io::Error::from_raw_os_error(-cmd.result));
    }
    Ok(cmd.result as usize)
}

pub fn kpm_module_info(name: &str, buf: &mut [u8]) -> std::io::Result<usize> {
    let name_c = CString::new(name).unwrap();

    let mut cmd = KsuKpmInfoCmd {
        name: name_c.as_ptr() as u64,
        buf: buf.as_mut_ptr() as u64,
        buf_size: buf.len() as u32,
        result: 0,
    };

    let ret = ksuctl(KSU_IOCTL_KPM_INFO, &raw mut cmd as *mut _)?;
    if ret < 0 {
        return Err(std::io::Error::last_os_error());
    }
    if cmd.result < 0 {
        return Err(std::io::Error::from_raw_os_error(-cmd.result));
    }
    Ok(cmd.result as usize)
}

pub fn kpm_module_control(name: &str, args: &str, out_buf: &mut [u8]) -> std::io::Result<i32> {
    let name_c = CString::new(name).unwrap();
    let args_c = CString::new(args).unwrap();

    let mut cmd = KsuKpmControlCmd {
        name: name_c.as_ptr() as u64,
        args: args_c.as_ptr() as u64,
        out_buf: out_buf.as_mut_ptr() as u64,
        out_len: out_buf.len() as i32,
        result: 0,
    };

    let ret = ksuctl(KSU_IOCTL_KPM_CONTROL, &raw mut cmd as *mut _)?;
    if ret < 0 {
        return Err(std::io::Error::last_os_error());
    }
    if cmd.result < 0 {
        return Err(std::io::Error::from_raw_os_error(-cmd.result));
    }
    Ok(cmd.result)
}

/// Public API used by cli.rs to load KPM from path
pub fn kpm_load(path: &str, args: Option<&str>) -> Result<()> {
    info!("KPM: loading module {}", path);
    let result = kpm_load_module(path, args.unwrap_or(""));
    match result {
        Ok(0) => {
            info!("KPM: loaded successfully: {}", path);
            Ok(())
        }
        Ok(-libc::EEXIST) => {
            info!("KPM: module already loaded (EEXIST)");
            Ok(())
        }
        _ => {
            let err = result.unwrap_or(-1);
            error!("KPM: load failed for {}, errno={}", path, -err);
            let msg = match -err {
                libc::ENOENT => "模块文件不存在",
                libc::ENOEXEC => "不是有效的 KPM 模块 (.kpm.init/.kpm.exit 段缺失)",
                libc::ENOMEM => "内存不足",
                libc::EEXIST => "模块已加载",
                libc::EINVAL => "参数无效",
                _ => "未知错误",
            };
            bail!("KPM load failed: {} ({}) - {}", err, msg, path);
        }
    }
}

pub fn kpm_unload(name: &str) -> Result<()> {
    info!("KPM: unloading module {}", name);
    match kpm_unload_module(name) {
        Ok(0) => {
            info!("KPM: unloaded successfully: {}", name);
            Ok(())
        }
        Ok(-libc::ENOENT) => {
            bail!("KPM unload failed: module not found: {}", name);
        }
        _ => {
            let err = kpm_unload_module(name).unwrap_or(-1);
            bail!("KPM unload failed: {} - {}", err, name);
        }
    }
}

pub fn kpm_list() -> Result<String> {
    let mut buf = vec![0u8; 4096];
    let n = kpm_module_list(&mut buf)?;
    if n == 0 {
        return Ok(String::from("(no modules loaded)"));
    }
    Ok(String::from_utf8_lossy(&buf[..n]).to_string())
}

pub fn kpm_info(name: &str) -> Result<String> {
    let mut buf = vec![0u8; 1024];
    let n = kpm_module_info(name, &mut buf)?;
    if n == 0 {
        return Ok(format!("(no info for module: {})", name));
    }
    Ok(String::from_utf8_lossy(&buf[..n]).to_string())
}

pub fn kpm_nums() -> Result<i32> {
    Ok(kpm_module_nums()?)
}

pub fn kpm_control(name: &str, args: &str) -> Result<i32> {
    let mut buf = vec![0u8; 256];
    match kpm_module_control(name, args, &mut buf) {
        Ok(rc) => {
            let out = String::from_utf8_lossy(&buf);
            if !out.trim().is_empty() {
                info!("KPM control {} response: {}", name, out.trim());
            }
            Ok(rc)
        }
        Err(e) => {
            bail!("KPM control failed for {}: {}", name, e);
        }
    }
}

pub fn kpm_diag() -> String {
    format!("KPM ioctl-based module loader ready. Use `kpm load <path>` to load a .kpm module.")
}
