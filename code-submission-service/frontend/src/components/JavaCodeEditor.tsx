import { useEffect, useRef } from "react";
import { basicSetup } from "codemirror";
import { java } from "@codemirror/lang-java";
import { Compartment, EditorState } from "@codemirror/state";
import { EditorView } from "@codemirror/view";

interface JavaCodeEditorProps {
  initialCode: string;
  readOnly: boolean;
  onCodeChange: (code: string) => void;
}

/**
 * CodeMirror 6 Java editor. Phase 1 records nothing but the code itself;
 * keystroke and paste capture arrive in Phase 3 as a separate CodeMirror extension.
 */
export function JavaCodeEditor({ initialCode, readOnly, onCodeChange }: JavaCodeEditorProps) {
  const containerReference = useRef<HTMLDivElement>(null);
  const editorViewReference = useRef<EditorView | null>(null);
  const readOnlyCompartment = useRef(new Compartment());
  const onCodeChangeReference = useRef(onCodeChange);
  onCodeChangeReference.current = onCodeChange;

  useEffect(() => {
    if (!containerReference.current) {
      return;
    }
    const editorView = new EditorView({
      parent: containerReference.current,
      state: EditorState.create({
        doc: initialCode,
        extensions: [
          basicSetup,
          java(),
          EditorState.tabSize.of(4),
          EditorView.contentAttributes.of({ "aria-label": "Java code editor" }),
          readOnlyCompartment.current.of(readOnlyExtensions(readOnly)),
          EditorView.updateListener.of((update) => {
            if (update.docChanged) {
              onCodeChangeReference.current(update.state.doc.toString());
            }
          }),
        ],
      }),
    });
    editorViewReference.current = editorView;
    return () => {
      editorView.destroy();
      editorViewReference.current = null;
    };
    // The editor is created once; later code changes come from the student, not from props.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    editorViewReference.current?.dispatch({
      effects: readOnlyCompartment.current.reconfigure(readOnlyExtensions(readOnly)),
    });
  }, [readOnly]);

  return <div className="code-editor" ref={containerReference} />;
}

function readOnlyExtensions(readOnly: boolean) {
  return [EditorState.readOnly.of(readOnly), EditorView.editable.of(!readOnly)];
}
