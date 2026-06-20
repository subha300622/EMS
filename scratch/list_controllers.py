import os, re, subprocess

result = subprocess.run(
    ['find', '/home/subashini/Documents/ems-backend/src/main/java', '-name', '*Controller.java', '-path', '*/controller/*'],
    capture_output=True, text=True
)
controller_files = [f.strip() for f in result.stdout.strip().split('\n') if f.strip()]

print(f"{'Controller':<35} | {'Base Path':<30} | {'Tag(s)':<30}")
print("-" * 100)

for path in sorted(controller_files):
    with open(path) as fh:
        content = fh.read()
        
    fname = os.path.basename(path)
    base_path = ''
    m = re.search(r'@RequestMapping\("([^"]+)"', content)
    if m:
        base_path = m.group(1)
        
    class_section = content.split('public class')[0] if 'public class' in content else content[:500]
    tags = re.findall(r'@Tag\(name\s*=\s*"([^"]+)"', content)
    unique_tags = list(dict.fromkeys(tags))
    
    tags_str = ", ".join(unique_tags) if unique_tags else 'NONE'
    print(f"{fname:<35} | {base_path:<30} | {tags_str:<30}")
