#!/usr/bin/env python3
import ast,subprocess,tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
APPLY=ROOT/"scripts/apply-v1447-r3-write-progress-r8.py"
tree=ast.parse(APPLY.read_text(encoding="utf-8"))
vals={}
for node in tree.body:
    if isinstance(node,ast.Assign):
        for t in node.targets:
            if isinstance(t,ast.Name) and t.id in {"OPS","NEW_FILES"}:
                vals[t.id]=ast.literal_eval(node.value)
OPS=vals["OPS"]; NEW_FILES=vals["NEW_FILES"]
tmp=Path(tempfile.mkdtemp(prefix="ytm-r8-"))
group={}
for op in OPS: group.setdefault(op["file"],[]).append(op["old"])
for rel,chunks in group.items():
    p=tmp/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text("\n\n".join(chunks)+"\n",encoding="utf-8",newline="\n")
(tmp/"scripts").mkdir(parents=True,exist_ok=True)
(tmp/"scripts"/APPLY.name).write_text(APPLY.read_text(encoding="utf-8"),encoding="utf-8",newline="\n")
def run(*args,expect=0):
    r=subprocess.run(["python","-B",f"scripts/{APPLY.name}",*args],cwd=tmp,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    if r.returncode!=expect: raise SystemExit(f"FAIL exit={r.returncode} expected={expect}\n{r.stdout}")
    return r.stdout
run("--check");print("PASS: clean --check")
run();print("PASS: first apply")
run();print("PASS: idempotent second apply")
for op in OPS:
    if (tmp/op["file"]).read_text(encoding="utf-8").count(op["new"])!=1: raise SystemExit(f"FAIL replacement {op['name']}")
for rel,content in NEW_FILES.items():
    if (tmp/rel).read_text(encoding="utf-8")!=content: raise SystemExit(f"FAIL generated {rel}")
print("PASS: R8 selftest complete")
