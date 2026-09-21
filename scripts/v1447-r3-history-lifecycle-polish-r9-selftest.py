#!/usr/bin/env python3
import ast, subprocess, tempfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
APPLY=ROOT/"scripts/apply-v1447-r3-history-lifecycle-polish-r9.py"

tree=ast.parse(APPLY.read_text(encoding="utf-8"))
vals={}
for node in tree.body:
    if isinstance(node,ast.Assign):
        for t in node.targets:
            if isinstance(t,ast.Name) and t.id in {"OPS","NEW_FILES"}:
                vals[t.id]=ast.literal_eval(node.value)

OPS=vals["OPS"]
NEW_FILES=vals["NEW_FILES"]

tmp=Path(tempfile.mkdtemp(prefix="ytm-r9-"))
grouped={}
for op in OPS:
    grouped.setdefault(op["file"],[]).append(op["old"])

for rel,chunks in grouped.items():
    p=tmp/rel
    p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text("\n\n".join(chunks)+"\n",encoding="utf-8",newline="\n")

(tmp/"scripts").mkdir(parents=True,exist_ok=True)
(tmp/"scripts"/APPLY.name).write_text(
    APPLY.read_text(encoding="utf-8"),
    encoding="utf-8",
    newline="\n",
)

def run(*args):
    r=subprocess.run(
        ["python","-B",f"scripts/{APPLY.name}",*args],
        cwd=tmp,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT
    )
    if r.returncode!=0:
        raise SystemExit(f"FAIL: exit={{r.returncode}}\n{{r.stdout}}")
    return r.stdout

run("--check")
print("PASS: clean --check")
run()
print("PASS: first apply")
run()
print("PASS: idempotent second apply")

for op in OPS:
    text=(tmp/op["file"]).read_text(encoding="utf-8")
    if op["new"]:
        if text.count(op["new"])!=1:
            raise SystemExit(f"FAIL replacement: {{op['name']}}")
    elif op["old"] in text:
        raise SystemExit(f"FAIL removal: {{op['name']}}")

for rel,content in NEW_FILES.items():
    if (tmp/rel).read_text(encoding="utf-8")!=content:
        raise SystemExit(f"FAIL generated file: {{rel}}")

print("PASS: R9 selftest complete")
