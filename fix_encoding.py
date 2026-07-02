import re

with open('d:\\KinSU\\kinsu-site\\index_decoded.html', 'r', encoding='utf-8') as f:
    content = f.read()

def encode_text(text):
    result = []
    for ch in text:
        if ord(ch) > 127:
            result.append(f'&#x{ord(ch):X};')
        else:
            result.append(ch)
    return ''.join(result)

output = []
pos = 0
while pos < len(content):
    script_match = re.search(r'<script\b', content[pos:], re.IGNORECASE)
    style_match = re.search(r'<style\b', content[pos:], re.IGNORECASE)
    
    next_tag = None
    tag_type = None
    if script_match:
        next_tag = pos + script_match.start()
        tag_type = 'script'
    if style_match:
        s_pos = pos + style_match.start()
        if next_tag is None or s_pos < next_tag:
            next_tag = s_pos
            tag_type = 'style'
    
    if next_tag is None:
        output.append(encode_text(content[pos:]))
        break
    
    output.append(encode_text(content[pos:next_tag]))
    
    if tag_type == 'script':
        end_match = re.search(r'</script\s*>', content[next_tag:], re.IGNORECASE)
    else:
        end_match = re.search(r'</style\s*>', content[next_tag:], re.IGNORECASE)
    
    if end_match is None:
        output.append(encode_text(content[next_tag:]))
        break
    
    end_pos = next_tag + end_match.end()
    output.append(content[next_tag:end_pos])
    pos = end_pos

fixed = ''.join(output)
with open('d:\\KinSU\\kinsu-site\\index_fixed.html', 'w', encoding='ascii') as f:
    f.write(fixed)

print(f'Fixed file size: {len(fixed)} bytes')
max_byte = max(fixed.encode('ascii'))
print(f'Max byte value: {max_byte}')
script_section = re.search(r'<script>(.*?)</script>', fixed, re.DOTALL|re.IGNORECASE)
if script_section:
    js = script_section.group(1)
    print(f'JS length: {len(js)}')
    print(f'JS has section check: {"if(section&&" in js}')
