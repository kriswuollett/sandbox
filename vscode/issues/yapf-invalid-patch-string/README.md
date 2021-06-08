Formatting the file `somemodule/test.py` in Visual Studio Code yields the following error in the Python console:

```
 /usr/local/bin/yapf --style=/Users/kris/workspace/github.com/kriswuollett/sandbox/vscode/issues/yapf-invalid-patch-string/.config/yapf --verbose --diff ~/workspace/github.com/kriswuollett/sandbox/vscode/issues/yapf-invalid-patch-string/somemodule/test.py
cwd: ~/workspace/github.com/kriswuollett/sandbox/vscode/issues/yapf-invalid-patch-string/somemodule

Formatting with yapf failed.
Error: Invalid patch string: Reformatting /Users/kris/workspace/github.com/kriswuollett/sandbox/vscode/issues/yapf-invalid-patch-string/somemodule/test.py
```

Running the command myself yields output like:

```
% /usr/local/bin/yapf --style=/Users/kris/workspace/github.com/kriswuollett/sandbox/vscode/issues/yapf-invalid-patch-string/.config/yapf --verbose --diff ~/workspace/github.com/kriswuollett/sandbox/vscode/issues/yapf-invalid-patch-string/somemodule/test.py
Reformatting /Users/kris/workspace/github.com/kriswuollett/sandbox/vscode/issues/yapf-invalid-patch-string/somemodule/test.py
--- /Users/kris/workspace/github.com/kriswuollett/sandbox/vscode/issues/yapf-invalid-patch-string/somemodule/test.py	(original)
+++ /Users/kris/workspace/github.com/kriswuollett/sandbox/vscode/issues/yapf-invalid-patch-string/somemodule/test.py	(reformatted)
@@ -8,4 +8,4 @@


 if __name__ == "__main__":
-    sys.exit    (main(sys.argv))
+    sys.exit(main(sys.argv))
```
