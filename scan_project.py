import os

# 需要过滤掉的无关目录（编译产物、IDE缓存、虚拟环境等）
IGNORE_DIRS = {'.git', '.idea', '.vscode', 'target', 'bin', 'out', '.venv', '__pycache__', 'results'}
# 需要过滤掉的无关文件后缀
IGNORE_EXTS = {'.class', '.jar', '.pyc', '.png', '.jpg', '.iml', '.md'}

def generate_tree(dir_path, prefix=""):
    tree_str = ""
    try:
        items = sorted(os.listdir(dir_path))
    except PermissionError:
        return ""

    # 过滤有效项
    valid_items = []
    for item in items:
        path = os.path.join(dir_path, item)
        if os.path.isdir(path) and item in IGNORE_DIRS:
            continue
        if os.path.isfile(path) and any(item.endswith(ext) for ext in IGNORE_EXTS):
            continue
        # 忽略脚本自身
        if item == "scan_project.py" or item == "project_summary.txt":
            continue
        valid_items.append(item)

    for i, item in enumerate(valid_items):
        is_last = (i == len(valid_items) - 1)
        connector = "└── " if is_last else "├── "
        tree_str += f"{prefix}{connector}{item}\n"
        
        path = os.path.join(dir_path, item)
        if os.path.isdir(path):
            extension = "    " if is_last else "│   "
            tree_str += generate_tree(path, prefix + extension)
    return tree_str

def read_file_content(root_dir, filename):
    filepath = os.path.join(root_dir, filename)
    if os.path.exists(filepath):
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                return f.read()
        except Exception as e:
            return f"Error reading {filename}: {e}"
    return f"{filename} not found."

def main():
    root_dir = "."
    output_file = "project_summary.txt"
    
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("=== PROJECT DIRECTORY TREE ===\n")
        f.write(".\n")
        f.write(generate_tree(root_dir))
        
        f.write("\n\n=== MAVEN POM.XML ===\n")
        f.write(read_file_content(root_dir, "pom.xml"))
        
    print(f"扫描完成！已生成摘要文件：{output_file}")

if __name__ == "__main__":
    main()