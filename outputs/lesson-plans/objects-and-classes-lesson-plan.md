---
Topic: Objects and Classes (Class Anatomy)
Course: CSCI1301
Session length: 85 minutes
Prior knowledge: Chapters 1-8 (selections, loops, methods, arrays, 2D arrays)
Research report: outputs/research/oop-mvc-javafx-research-report.md (reused; no new research)
Resources used: resources/CSCI1301/slides/09slide_custom1.pptx
Date: 2026-11-07
---

# Objects and Classes in Java — Lesson Plan

## Learning Goals
By the end of class, students can:
1. **Distinguish** a class (the blueprint) from an object (one instance built from it).
2. **Read and draw** a UML class diagram showing data fields, constructors, and methods.
3. **Write** a class with data fields, a no-arg constructor, an overloaded constructor, and instance methods.
4. **Create and use** objects with `new`, a reference variable, and the dot operator.

## Key Terms
| Term | Definition |
|---|---|
| Object | One distinct thing in a program, with its own **state** (data) and **behavior** (actions) |
| Class | The blueprint that defines what data and behavior every object of that type has |
| Instance | Another word for object: one specific copy made from a class |
| Instantiation | The act of creating an object from a class |
| Data field | A variable declared in a class, not inside a method; it holds an object's state. Also called an *instance variable* |
| Instance method | A method that belongs to an object and works with that object's data fields |
| Constructor | A special method that runs once, when an object is created, to set its starting state |
| No-arg constructor | A constructor with no parameters |
| Default constructor | An empty no-arg constructor Java adds for you, **only** if you write no constructors |
| `new` | The operator that creates an object in memory and calls a constructor |
| Reference variable | A variable that holds the location of an object, not the object itself |
| Dot operator (`.`) | Reaches through a reference variable to a field or method: `door.getArea()` |
| UML class diagram | A three-box drawing of a class: name, data fields, then constructors and methods |

## Concept Explanation
**Why classes exist.** Chapters 1-8 gave students variables, loops, methods, and arrays. Those tools can't keep related data and behavior together. A program about 30 students would need 30 names, 30 GPAs, and loose methods that take all of them as arguments. A class bundles the data and the methods that act on it into one new type.

**Class vs. object.** A **class** is a recipe; an **object** is a pie baked from it. You write the class once and bake as many pies as you need. Each object has its own copy of the data fields, so two `Rectangle` objects can have different widths.

**State and behavior.** Every object has *state* (what it knows, stored in data fields) and *behavior* (what it does, written as instance methods). A circle knows its radius and can compute its area.

**UML first, code second.** A UML class diagram is a design sketch. It has three boxes: the class name, the data fields (`width: double`), and the constructors and methods (`getArea(): double`). Students should draw the diagram before they code it.

**Constructors.** A constructor has three rules: it **has the same name as the class**, it **has no return type, not even `void`**, and it **runs only when `new` is called**. Overloading lets a class offer more than one way to build an object.

**The default constructor trap.** If you write *no* constructor, Java adds an empty no-arg one for you. If you write *any* constructor, Java adds nothing. Writing only `Rectangle(double, double)` makes `new Rectangle()` fail to compile.

**Reference variables and the dot.** `Rectangle door = new Rectangle(3, 7);` does three things: it declares a reference variable, it creates the object, and it stores the object's location in the variable. Students have already done this: `Scanner input = new Scanner(System.in);`. The dot operator then reaches into that object, as in `door.width` or `door.getArea()`.

**Instance vs. class calls.** `Math.pow(2, 3)` works because `pow` is static. `Rectangle.getArea()` does not compile, because `getArea()` needs a specific rectangle to measure. Static members are covered in a later lesson.

> **Scope:** Fields here use package (default) access so students can see the dot operator on fields. `private`, getters, and setters belong to the encapsulation lesson (`outputs/lesson-plans/encapsulation-lesson-plan.md`).

## Code Example
```java
// File: TestRectangle.java. Verified with javac 25.
// Client class: holds main, creates and uses Rectangle objects.
public class TestRectangle {
    public static void main(String[] args) {
        Rectangle defaultRect = new Rectangle();          // no-arg constructor
        Rectangle door = new Rectangle(3.0, 7.0);         // overloaded constructor

        System.out.println("Default area: " + defaultRect.getArea());
        System.out.println("Door area: " + door.getArea());

        door.width = 4.0;                                 // dot operator: field
        System.out.println("Door perimeter: " + door.getPerimeter()); // dot operator: method
    }
}

// Blueprint class: data fields + constructors + instance methods.
class Rectangle {
    double width = 1.0;     // data fields (state)
    double height = 1.0;

    Rectangle() {           // no-arg constructor: keeps the defaults
    }

    Rectangle(double newWidth, double newHeight) {  // same name, no return type
        width = newWidth;
        height = newHeight;
    }

    double getArea() {      // instance methods (behavior)
        return width * height;
    }

    double getPerimeter() {
        return 2 * (width + height);
    }
}
```

**Output:**
```
Default area: 1.0
Door area: 21.0
Door perimeter: 22.0
```

**What to point out:**
- `Rectangle(double newWidth, double newHeight)` has **no return type**. Add `void` live and show that it becomes an ordinary method, so `new Rectangle(3.0, 7.0)` stops compiling.
- `defaultRect` and `door` are **two objects from one class**, and each keeps its own `width` and `height`.
- Delete the no-arg constructor live. `new Rectangle()` breaks, which shows the default-constructor trap.

**UML to sketch on the board:**
```
+-----------------------------------------+
| Rectangle                               |
+-----------------------------------------+
| width: double                           |
| height: double                          |
+-----------------------------------------+
| Rectangle()                             |
| Rectangle(newWidth: double,             |
|           newHeight: double)            |
| getArea(): double                       |
| getPerimeter(): double                  |
+-----------------------------------------+
```

## Agenda
| Time | Activity | App activity ID |
|---|---|---|
| 0-20 | Concepts: why classes, blueprint vs. object, state and behavior, UML, constructor rules | s1 |
| 20-40 | Live-code `Rectangle` + `TestRectangle`: draw the UML, write the class, break it on purpose (`void`, missing no-arg) | — |
| 40-60 | Students build `Book` from a UML diagram and submit | c1 |
| 60-75 | Pairs explain their constructor and trace one method call | p1 |
| 75-85 | Exit ticket | q1 |
| **Total** | **85 min** | |

## Practice
Students receive this UML diagram and write `Book`, then run the provided `TestBook.main`:

```
Book
-------------------------------
title: String = "Untitled"
pages: int = 1
pagesRead: int = 0
-------------------------------
Book()
Book(newTitle: String, newPages: int)
getPercentRead(): double
describe(): String
```

`getPercentRead()` returns `pagesRead * 100.0 / pages`. `describe()` returns `title (pages pages)`.

**Expected output:**
```
Untitled (1 pages)
Dune (412 pages)
Percent read: 25.0%
```

**Common errors to watch for:** writing `void Book(...)`, integer division (`pagesRead * 100 / pages`), and calling `Book.describe()` instead of `novel.describe()`.

## Exit Ticket
Five questions in the app (`q1`), covering:
1. Class vs. object (Goal 1)
2. Reading a UML diagram (Goal 2)
3. The default-constructor trap (Goal 3)
4. Why `Rectangle.getArea()` fails (Goal 4)
5. What a constructor's header looks like (Goal 3)

## Sources
1. *Introduction to Java Programming and Data Structures*, 13th ed., Chapter 9: Objects and Classes — Y. Daniel Liang, Pearson (2024); course slides at `resources/CSCI1301/slides/09slide_custom1.pptx`, slides 11-48
2. Object-Oriented Programming Concepts — Oracle Java Tutorials, https://docs.oracle.com/javase/tutorial/java/concepts/
3. OOP and MVC in JavaFX research report — `outputs/research/oop-mvc-javafx-research-report.md`, Section 1
