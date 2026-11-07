# Encapsulation in Java — Lesson Plan

**Session:** 75 minutes · **Level:** Intro Java · **Workflow:** `lesson-planning-workflow.md`

## Learning Goals
By the end of class, students can:
- Define **encapsulation**: bundling data with the methods that control it, and hiding that data from outside code.
- Use `private` fields with public **getters** and **setters**.
- Explain *why* validation belongs inside the class.

## Key Terms
| Term | Definition |
|---|---|
| Access modifier | Keyword (`private`, `public`) that controls visibility |
| Getter | Method that returns a field's value |
| Setter | Method that changes a field's value, often with validation |

## Code Example
```java
public class BankAccount {
    private double balance;

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }
}
```

## Agenda
| Time | Activity |
|---|---|
| 0–15 | Concept and real-world analogy (ATM vs. vault) |
| 15–40 | Live-code `BankAccount` |
| 40–65 | Pair practice: encapsulate a `Student` class |
| 65–75 | Exit ticket: "What breaks if `balance` is public?" |
