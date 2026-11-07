# Resources

Reference material that the research and lesson-plan workflows check **before** they search the web.

## Layout
Organized **by course, then material type**:

```
resources/
├── CSCI1301/   slides/  assignments/  labs/  practice/
├── CSCI1302/   slides/  assignments/  labs/  practice/
└── IT2531/     slides/  assignments/  labs/  practice/
```

| Folder | Holds |
|---|---|
| `slides/` | Lecture decks (`.pptx`, `.pdf`) |
| `assignments/` | Graded assignments and rubrics (`.docx`, `.pdf`) |
| `labs/` | In-class or lab instructions |
| `practice/` | Practice problems, worksheets, examples |

Course-wide items (syllabi, trusted docs, textbook excerpts) go at the top of the course folder, such as `CSCI1301/csci1301-syllabus.pdf`.

## Search Order
1. `resources/<course>/` for the course being planned.
2. All of `/resources`.
3. Then the web.

## Naming
Use descriptive, hyphenated names that start with the chapter when there is one: `ch04-encapsulation-slides.pptx`, `lab03-getters-setters.docx`.
