---
Topic: Object-Oriented Programming and the Model-View-Controller Pattern in JavaFX
Audience: Intro CS students (main text) and instructor (Instructor Notes)
Depth: Working knowledge
Date: 2026-11-07
Sources: 12 web sources + ILEP source code
---

# OOP and MVC in JavaFX: From Pillars to a Working App

## Summary
Object-oriented programming (OOP) gives you four tools: encapsulation, inheritance, polymorphism, and abstraction. Model-View-Controller (MVC) is a way of *using* those tools to keep a GUI program organized. The **model** holds the data and rules, the **view** draws the screen, and the **controller** connects the two. JavaFX supports MVC directly through observable properties, observable lists, and FXML controllers. ILEP is a working example: it splits its code into `models/`, `views/`, and `controllers/`, and a shared `AppState` object lets controllers coordinate without calling each other.

## Key Terms
| Term | Definition |
|---|---|
| Encapsulation | Hiding an object's data behind methods that control access to it [1] |
| Inheritance | A subclass acquires the fields and methods of its superclass [1][2] |
| Polymorphism | One reference type, many runtime behaviors; a `Parent` variable can hold any view |
| Abstraction | Exposing *what* something does while hiding *how*; interfaces are the main tool [2] |
| Interface | A contract: a class that implements it promises to provide its methods [2] |
| Design pattern | A named, reusable solution to a recurring design problem |
| Separation of concerns | Each part of a program has one job and does not do the others' jobs |
| Observer pattern | Objects register as listeners and are notified when something changes [5] |
| Property (JavaFX) | An observable value; listeners and bindings react when it changes [8] |
| Binding | A declared relationship that keeps one value in sync with another [8] |
| JavaFX Application Thread | The single thread that is allowed to touch the live UI [12] |

## Findings

### 1. The four OOP pillars in Java
- **Encapsulation.** Oracle defines it as hiding internal state and requiring all interaction to go through an object's methods [1]. In Java, that means `private` fields plus public methods that can validate input.
- **Inheritance.** A class `extends` one superclass and inherits its members. Every class ultimately descends from `Object` [1][2].
- **Polymorphism.** A superclass or interface reference can point to any subtype, and the method that runs is chosen at runtime. Overriding `toString()` is the simplest example.
- **Abstraction.** Interfaces describe behavior without implementation [2]. Java's functional interfaces (`Runnable`, `Consumer<T>`, `Predicate<T>`) are small abstractions you can pass around as lambdas or method references.

> **Note:** Oracle's tutorials were written for JDK 8. The concepts are unchanged, but dev.java hosts the updated material [1].

### 2. What MVC is and where it came from
Trygve Reenskaug formulated MVC in 1978–79 as a visiting scientist at Xerox PARC [3][4]. His May 1979 note used four parts (Thing, Model, View, Editor). By December 1979 the report was titled "Models-Views-Controllers" [3][4].

Martin Fowler calls MVC "one of the most misunderstood" patterns, because many different designs carry the name [5]. He reduces it to two durable principles:
1. **Separated presentation:** domain logic (the model) does not depend on the screen.
2. **Observer synchronization:** screens update by reacting to change events [5].

| Layer | Responsibility | Must not |
|---|---|---|
| Model | Data, rules, persistence | Import JavaFX UI classes |
| View | Layout and display; report user actions | Change the model |
| Controller | Handle events, update the model, refresh the view | Contain layout code |

Two common variants matter for JavaFX [6][7]:
- **Passive View.** The controller pushes every update into a "dumb" view. Tests are easiest, because a fake view can stand in for the real one [6].
- **Supervising Controller.** The view handles simple data binding itself, and the controller handles the complex logic. You write less code but test less of the UI [7].

### 3. How JavaFX supports MVC
- **Properties and listeners.** `SimpleObjectProperty`, `SimpleBooleanProperty`, and similar classes notify listeners on change. Binding expresses direct relationships between values [8].
- **Observable collections.** `ObservableList` reports adds and removes. Content bindings keep two lists in sync [9].
- **FXML.** You declare layout in XML and name a controller with `fx:controller`. `FXMLLoader` creates the controller, injects `@FXML` fields that match `fx:id` attributes, and then calls `initialize()` [10][11].
- **Code-built views.** FXML is optional. You can build views as Java classes that extend layout panes, which is the approach ILEP takes.
- **Threading rule.** UI changes must happen on the JavaFX Application Thread. From a background thread, use `Platform.runLater(...)` [12].

> **Note:** In FXML, the "controller" class is often closer to a *view-controller*: it holds widget references. That is why many apps still keep a separate model and app-level controller.

### 4. How ILEP applies MVC and OOP
ILEP follows a **Passive View / Supervising Controller** style: views never read or change models, and controllers push data into views [13].

```
App (Application) → AppController → creates RosterManager (model) + AppState (shared state)
                                  → creates one controller per screen
AttendanceView  --(setOnMarkChanged callback)-->  AttendanceController
AttendanceController  --(session.mark)-->  Attendance (model)
AttendanceController  --(view.setCounts)-->  AttendanceView
```

| Pillar | Where it appears in ILEP |
|---|---|
| Encapsulation | `Student` has all-`private` fields with getters and setters. `AppState` exposes `selectedRosters` only as an `unmodifiableObservableList` and `currentScreen` as a `ReadOnlyObjectProperty`, so outside code cannot bypass `navigateTo()`. |
| Inheritance | `App extends Application`. `AttendanceView extends BorderPane`, `HomeView extends FlowPane`, `MenuBarView extends HBox`. |
| Polymorphism | `mainView.setContent(controller.getView())` accepts any view through the `Parent` type. `Student` and `ClassActivity` override `toString()`, so `ListView` shows readable names. |
| Abstraction | Views expose callbacks typed as `Runnable`, `Consumer<List<Roster>>`, and `BiConsumer<Student, Boolean>`. The view never knows which controller is listening. The controller passes `session::isPresent` as a `Predicate`. |

**Coordination through `AppState`.** Controllers never call each other. They change `AppState`, and they listen to it:

```java
// AttendanceController: re-render whenever a roll call starts or ends anywhere
state.attendanceSessionProperty().addListener(
        (obs, oldSession, newSession) -> render(newSession));
```

This is the observer principle Fowler describes [5]. A new screen only needs a reference to `AppState`, not to every other controller.

### 5. Common mistakes and how to test each layer
| Mistake | Why it hurts | Fix |
|---|---|---|
| Event handler edits the model inside the view | The UI and the rules become tangled and untestable | The view raises an event, and the controller updates the model |
| Model imports JavaFX controls | The model can't run or be tested without a GUI | Keep `javafx.scene.*` out of `models/` |
| Controllers call each other | Changes ripple unpredictably | Share state through one observable object (`AppState`) |
| `new MyController()` alongside FXML | `@FXML` fields stay `null`, because only `FXMLLoader` injects them [10] | Let `FXMLLoader` create it, or call `setController` |
| Long work on the UI thread | The window freezes | Use a background `Task` and `Platform.runLater` for UI updates [12] |

**Testing strategy:**
- **Models:** plain JUnit tests. No JavaFX is needed.
- **Controllers:** give the controller a fake view. This requires views to implement an interface, which is the main testability benefit of Passive View [6].
- **Views:** test them manually or with a UI-testing library. Keep them thin, so there is little to test.

## Practical Applications
**Walkthrough: marking a student present in ILEP**
1. The user ticks a checkbox in `AttendanceView`.
2. The view calls its `onMarkChanged` handler with `(student, true)`. It does not touch the model.
3. `AttendanceController.handleMarkChanged` calls `session.mark(student, present)` on the `Attendance` model.
4. The controller calls `view.setCounts(present, absent, total)` to refresh the totals.

**Template for adding a new screen (e.g., Grades):**
```java
// models/Gradebook.java – pure Java, no JavaFX
// views/GradesView.java
public class GradesView extends BorderPane {
    private Consumer<Student> onStudentSelected;
    public void setOnStudentSelected(Consumer<Student> handler) { onStudentSelected = handler; }
    public void showGrades(List<Student> students) { /* build controls */ }
}
// controllers/GradesController.java
public class GradesController {
    private final GradesView view = new GradesView();
    public GradesController(AppState state) {
        view.setOnStudentSelected(this::handleSelect);
        List<Student> students = new ArrayList<>();
        state.getSelectedRosters().forEach(r -> students.addAll(r.getStudents()));
        view.showGrades(students); // works even when no roster is selected
    }
    public Parent getView() { return view; }
}
```
Then add `GRADES` to `AppState.Screen` and a `case` to `AppController.show()`.

## Instructor Notes
**Suggested teaching order**
1. Encapsulation with `Student`, using a `private` field and a validated setter.
2. Inheritance with "a view *is a* `BorderPane`."
3. Polymorphism with `toString()` and `setContent(Parent)`.
4. Abstraction with callbacks (`Runnable`, `Consumer`) before you introduce interfaces formally.
5. MVC, using the walkthrough above as a trace exercise.
6. `AppState` and listeners, as the observer pattern.

**Misconceptions to address**
- "MVC means three folders." No. It is a rule about *who may talk to whom*.
- "The FXML controller is the controller." It is often just view logic.
- "Getters and setters are encapsulation." Only when the setter enforces a rule.

**ILEP refactoring opportunities (good class exercises)**
- `Attendance` still carries legacy console code: a `Scanner(System.in)` field and package-private fields (`studentList`, `present`, `absent`). Making these `private` and moving console input out of the model would strengthen both encapsulation and MVC.
- `RosterManager` uses `static` counters (`numberOfRosters`). Ask students why shared static state is risky when two managers exist.
- `App.IMAGES_FOLDER` is a hard-coded absolute path. Moving it to configuration or a relative resource is a good portability exercise.
- The `tests/` classes are console `main()` demos. Converting `AttendanceTest` to JUnit tests of `mark`, `markAll`, and `getPresentStudents` is a strong first unit-testing assignment.

**Exercises**
1. Trace the "Mark All Present" button from click to updated counts, and label each step M, V, or C.
2. Add a `Late` status to `Attendance` without touching any view code. Then explain what had to change in the view, and why.
3. Extract an `AttendanceDisplay` interface from `AttendanceView`, and write a fake that implements it to test `AttendanceController`.

## Open Questions
- Should ILEP move to FXML? It would separate layout from code, but it would add a second language and the injection rules.
- Would a Presentation Model or MVVM style, with views binding directly to observable properties, reduce controller code without hurting testability [5][7]?
- How much testing value would view interfaces add, compared with the extra code for a course-sized app?

## Sources
1. Object-Oriented Programming Concepts — Oracle Java Tutorials, https://docs.oracle.com/javase/tutorial/java/concepts/
2. Learning the Java Language (Interfaces and Inheritance) — Oracle Java Tutorials, https://docs.oracle.com/javase/tutorial/java/index.html
3. The original MVC reports — T. Reenskaug, University of Oslo DUO archive, https://duo.uio.no/handle/10852/9621
4. Trygve Reenskaug — Wikipedia, https://en.wikipedia.org/wiki/Trygve_Reenskaug
5. GUI Architectures — Martin Fowler (2006), https://www.martinfowler.com/eaaDev/uiArchs.html
6. Passive View — Martin Fowler (2006), https://www.martinfowler.com/eaaDev/PassiveScreen.html
7. Supervising Controller — Martin Fowler (2006), https://www.martinfowler.com/eaaDev/SupervisingPresenter.html
8. Using JavaFX Properties and Binding — Oracle, https://docs.oracle.com/javafx/2/binding/jfxpub-binding.htm
9. ListProperty (JavaFX 17 API) — OpenJFX, https://openjfx.io/javadoc/17/javafx.base/javafx/beans/property/ListProperty.html
10. FXML — dev.java, https://dev.java/learn/javafx/fxml
11. FXML Tutorial: JavaFX 2.x Features — Oracle, https://docs.oracle.com/javase/8/javafx/fxml-tutorial/jfx2_x-features.htm
12. Platform (JavaFX API, Java 9 Javadoc mirror) — SciJava, https://javadoc.scijava.org/Java9/javafx/application/Platform.html
13. ILEP source code — `src/controllers/AppState.java`, `src/controllers/AppController.java`, `src/controllers/AttendanceController.java`, `src/views/AttendanceView.java`, `src/models/Attendance.java`, `src/models/Student.java`, `src/models/ClassActivity.java`, `src/app/App.java`
