// End-to-end check of the student flow in a real browser (Chromium via Playwright).
//
//   node e2e/student-flow.cjs <service-url> <join-code> [screenshot-directory]
//
// Needs a running service and a SYNTHETIC class from scripts/create-synthetic-class.sh.
// Uses synthetic Eagle IDs only (900000001-900000005).
const { chromium } = require("playwright");
const assert = require("node:assert/strict");
const path = require("node:path");

const [serviceUrl, joinCode, screenshotDirectory] = process.argv.slice(2);
if (!serviceUrl || !joinCode) {
  console.error("Usage: node e2e/student-flow.cjs <service-url> <join-code> [screenshot-directory]");
  process.exit(2);
}

const loopSolution = [
  "import java.util.Scanner;",
  "",
  "public class Main {",
  "    public static void main(String[] arguments) {",
  "        Scanner scanner = new Scanner(System.in);",
  "        int count = scanner.nextInt();",
  "        for (int number = 1; number <= count; number++) {",
  "            System.out.println(number);",
  "        }",
  "    }",
  "}",
  "",
].join("\n");

async function replaceEditorCode(page, code) {
  await page.locator(".cm-content").click();
  await page.keyboard.press("ControlOrMeta+A");
  await page.keyboard.press("Delete");
  // insertText behaves like a paste-free edit and avoids auto-closing brackets doubling up.
  await page.keyboard.insertText(code);
}

async function screenshot(page, name) {
  if (screenshotDirectory) {
    await page.screenshot({ path: path.join(screenshotDirectory, `${name}.png`), fullPage: true });
  }
}

(async () => {
  const browser = await chromium.launch();
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 }, deviceScaleFactor: 2 });
  const page = await context.newPage();
  const consoleErrors = [];
  page.on("console", (message) => message.type() === "error" && consoleErrors.push(message.text()));

  // 1. Join: the join code is prefilled from the link; a wrong ID is rejected.
  await page.goto(`${serviceUrl}/?joinCode=${encodeURIComponent(joinCode)}`);
  assert.equal(await page.getByLabel("Join code").inputValue(), joinCode);
  await page.getByLabel("Eagle ID").fill("999999999");
  await page.getByRole("button", { name: "Join" }).click();
  await page.getByRole("alert").waitFor();
  assert.match(await page.getByRole("alert").innerText(), /don't match/);
  await screenshot(page, "1-join-page-wrong-id");

  await page.getByLabel("Eagle ID").fill("900000002");
  await page.getByRole("button", { name: "Join" }).click();

  // 2. Notice: must be acknowledged before the editor opens.
  await page.getByRole("heading", { name: "Before you start" }).waitFor();
  assert.ok(await page.getByText("AI restricted").isVisible());
  const continueButton = page.getByRole("button", { name: "Continue to the editor" });
  assert.ok(await continueButton.isDisabled());
  await screenshot(page, "2-notice-page");
  await page.getByLabel("I understand").check();
  await continueButton.click();

  // 3. Editor: starter code is loaded.
  await page.getByRole("heading", { name: "Lab 4: Counting with Loops" }).waitFor();
  assert.match(await page.locator(".cm-content").innerText(), /Write your loop here/);

  // 4. A compile error is shown clearly.
  await replaceEditorCode(page, "public class Main {\n    public static void main(String[] arguments) {\n        int count = 5\n    }\n}\n");
  await page.getByRole("button", { name: "Run ▶" }).click();
  await page.getByText("Compile errors").waitFor({ timeout: 30_000 });
  assert.match(await page.locator(".output-error").innerText(), /';' expected/);

  // 5. A correct program runs with standard input.
  await replaceEditorCode(page, loopSolution);
  await page.getByLabel("Program input (standard input)").fill("3");
  await page.getByRole("button", { name: "Run ▶" }).click();
  await page.getByText("exit code 0").waitFor({ timeout: 30_000 });
  assert.equal((await page.locator(".output-text").first().innerText()).trim(), "1\n2\n3");
  await page.getByText(/^Saved /).waitFor({ timeout: 15_000 });
  await screenshot(page, "3-editor-after-run");

  // 6. Autosave: an edit is saved without pressing anything.
  await page.locator(".cm-content").click();
  await page.keyboard.press("ControlOrMeta+End");
  await page.keyboard.insertText("// autosave check\n");
  await page.getByText("Unsaved changes").waitFor();
  await page.getByText(/^Saved /).waitFor({ timeout: 15_000 });

  // 7. Reloading keeps the work (token in sessionStorage, code on the server).
  await page.reload();
  await page.getByRole("heading", { name: "Lab 4: Counting with Loops" }).waitFor();
  assert.match(await page.locator(".cm-content").innerText(), /autosave check/);

  // 8. Submit locks the editor.
  await page.getByRole("button", { name: "Submit" }).click();
  await page.getByRole("heading", { name: "Submit your code?" }).waitFor();
  await page.getByRole("dialog").getByRole("button", { name: "Submit" }).click();
  await page.getByText(/Your code is now read-only/).waitFor();
  assert.equal(await page.locator(".cm-content").getAttribute("contenteditable"), "false");
  assert.ok(await page.getByRole("button", { name: "Submitted" }).isDisabled());
  assert.ok(await page.getByRole("button", { name: "Run ▶" }).isDisabled());
  await screenshot(page, "4-editor-submitted");

  assert.deepEqual(consoleErrors.filter((text) => !text.includes("401")), [], "browser console errors");
  await browser.close();
  console.log("Student flow passed: join, notice, compile error, run with input, autosave, reload, submit.");
})().catch((error) => {
  console.error(error);
  process.exit(1);
});
