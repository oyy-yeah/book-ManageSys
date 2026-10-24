# -*- coding: utf-8 -*-
"""
代码规模统计脚本（用于实践报告 4.3 节）。

统计口径：
  * 只统计团队自行编写的代码：Java 源码、自写前端 JS、页面 HTML、MyBatis 映射 XML、
    数据库脚本 SQL、应用配置 yml；
  * 不统计第三方库与插件（static/plugins、target 构建产物、node_modules 等）；
  * "总行数"包含注释与空行；"有效代码行数"剔除纯注释行与空行；
  * 单元测试代码单独统计，不计入业务有效代码。

使用：python scripts/code_stat.py
"""
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

COMMENT_PREFIX = ("//", "/*", "*", "<!--", "#", "--")


def count_lines(path):
    total = 0
    effective = 0
    for line in open(path, encoding="utf-8", errors="ignore"):
        total += 1
        text = line.strip()
        if not text:
            continue
        if text.startswith(COMMENT_PREFIX):
            continue
        effective += 1
    return total, effective


def collect():
    groups = {
        "Java 业务代码": [],
        "Java 测试代码": [],
        "前端 JS（自写）": [],
        "页面 HTML": [],
        "MyBatis 映射 XML": [],
        "数据库 SQL": [],
        "应用配置 YML": [],
    }
    for root, dirs, files in os.walk(os.path.join(ROOT, "src")):
        dirs[:] = [d for d in dirs if d not in ("target", "node_modules")]
        for name in files:
            path = os.path.join(root, name)
            rel = os.path.relpath(path, ROOT).replace("\\", "/")
            if "static/plugins" in rel:
                continue
            ext = os.path.splitext(name)[1].lower()
            if ext == ".java":
                groups["Java 测试代码" if "/test/" in rel else "Java 业务代码"].append(path)
            elif ext == ".js":
                groups["前端 JS（自写）"].append(path)
            elif ext == ".html":
                groups["页面 HTML"].append(path)
            elif ext == ".xml" and "mapper" in rel:
                groups["MyBatis 映射 XML"].append(path)
            elif ext == ".yml":
                groups["应用配置 YML"].append(path)
    for folder in (ROOT, os.path.join(ROOT, "sql")):
        if not os.path.isdir(folder):
            continue
        for name in os.listdir(folder):
            if name.endswith(".sql"):
                groups["数据库 SQL"].append(os.path.join(folder, name))
    return groups


def main():
    groups = collect()
    print("| 模块 | 文件数 | 代码行数 | 有效代码行数 |")
    print("| --- | ---: | ---: | ---: |")
    business_effective = 0
    total_lines = 0
    for name, paths in groups.items():
        lines = effective = 0
        for path in paths:
            l, e = count_lines(path)
            lines += l
            effective += e
        print("| %s | %d | %d | %d |" % (name, len(paths), lines, effective))
        if name != "Java 测试代码":
            business_effective += effective
            total_lines += lines
    print("| **合计（不含测试）** | - | %d | %d |" % (total_lines, business_effective))


if __name__ == "__main__":
    main()
