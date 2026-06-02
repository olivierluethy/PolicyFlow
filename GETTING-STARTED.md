# PolicyFlow — A Simple Guide

A plain-language guide to what this project is, how to run it on your own computer,
and the real problems we ran into while building it (and how we solved them).
No coding experience needed to follow along.

---

## 1. What is PolicyFlow?

PolicyFlow is a web application for **Swiss insurance brokers** — the people who help
customers choose a health-insurance plan. Think of it as the "office software" a
brokerage would use every day. It lets a broker:

- 👥 **Manage customers** — add, edit, search and view client details.
- 🧮 **Calculate premiums** — work out the monthly price of an insurance plan based on
  the customer's age, their canton (Swiss region), the deductible they choose, and
  whether they want accident cover. Swiss insurance prices really do depend on all of these.
- 📄 **Create offers** — generate a price quote for a customer.
- ✅ **Turn offers into policies** — when a customer says yes, the offer becomes a real
  contract with its own policy number.
- 📅 **Schedule appointments** — keep track of meetings with clients.
- 🖨️ **Download PDFs** — produce a professional PDF for any offer or policy.
- 📝 **Keep an audit log** — every action is recorded automatically, so there's a full
  history of who did what and when.

It has a polished **dark-themed dashboard** you log into, just like real business software.

### What it's made of (in simple terms)

- A **"backend"** (the engine) written in **Java** — it does the calculations, stores
  the data, and enforces the rules.
- A **database** (**PostgreSQL**) — a digital filing cabinet where all the customers,
  offers and policies are stored.
- A **"frontend"** (the screen you see) — a single web page that runs in your browser.
- **Docker** — a tool that packages the database (and optionally the app) so they run
  the same way on any computer, without a complicated install.

---

## 2. What you need installed

Just two things:

1. **Docker** (also called Docker Desktop) — runs the database in a tidy, self-contained box.
2. **Java 21** — runs the application engine. (Only needed for the "developer" way below.
   If you use the all-in-one Docker way, Docker handles Java for you.)

---

## 3. How to run it

There are **two ways**. Pick **one** — don't run both at once (they'd fight over the
same "door", see Problem #1 below).

### Option A — The easy, all-in-one way (recommended for a quick look)

This runs everything (database **and** app) inside Docker. Open a terminal in the
project folder and type:

```bash
docker compose up -d --build
```

- `docker compose` = start the pre-defined set of programs.
- `up` = turn them on. `-d` = run quietly in the background. `--build` = build the app first.

Wait about a minute, then open your web browser at:

👉 **http://localhost:8090**

To stop everything later:

```bash
docker compose down
```

### Option B — The "developer" way (live reload while editing code)

This runs only the database in Docker, and the app directly on your machine so that
code changes appear instantly.

```bash
docker compose up -d postgres   # start ONLY the database
./mvnw quarkus:dev              # start the app in live-edit mode
```

Then open the same address:

👉 **http://localhost:8090**

To stop it, press `Ctrl + C` in the terminal, then `docker compose down`.

### Logging in

| Username  | Password     | Role             |
|-----------|--------------|------------------|
| `admin`   | `admin123`   | Administrator    |
| `advisor` | `advisor123` | Insurance advisor |

> ⚠️ If the login fails, **clear the username/password boxes and type them in by hand**
> (see Problem #4 — your browser's password manager can secretly fill in the wrong ones).

---

## 4. Problems we hit, and how we fixed them

This is the honest "behind the scenes". Every one of these cost real time, and each
taught us something. The error messages are shown exactly as they appeared.

### Problem #1 — Two programs fighting over the same "door" (port 8080)

**The error:**
```
failed to bind host port 0.0.0.0:8080/tcp: address already in use
... Port already bound: 8080: Address already in use
```

**What it meant to us:** A computer reaches programs through numbered "doors" called
**ports**. Our app wanted door **8080**, but another project already running on the same
machine was sitting on it. Two programs can't share one door.

**How we fixed it:** We moved PolicyFlow to a free door, **8090**, instead.

**Lesson learned:** When something "is already in use", the program isn't broken — it just
needs a free spot. On a machine running several projects, give each one its own port.

---

### Problem #2 — Accidentally trying to start the app twice

**What happened:** Our first instructions said to run `docker compose up` (which started
the app) **and then** the developer command (which started the app *again*). Both wanted
door 8080, so the second one failed instantly.

**What it meant to us:** We were launching the same thing two ways at once.

**How we fixed it:** We made it clear there are **two separate ways to run it — choose one**
(Option A *or* Option B above), never both together.

**Lesson learned:** Be explicit in setup instructions. "Do A and B" can accidentally mean
"do the same thing twice." Spell out the either/or.

---

### Problem #3 — The database door was also taken (port 5432)

**The error:**
```
Bind for 0.0.0.0:5432 failed: port is already allocated
```

**What it meant to us:** Same idea as Problem #1, but for the **database**. Another
project's database was already using the database's usual door, **5432**.

**How we fixed it:** We moved our database to door **5433**.

**Lesson learned:** It's not just the app — every part (app, database) needs its own free
door. Check them all.

---

### Problem #4 — "Login failed" even though the password was correct

**The error (in the browser):**
```
POST http://localhost:8090/api/auth/login 401 (Unauthorized)
```

**What it meant to us:** "401 Unauthorized" means *"wrong username or password."* But we
tested the login directly and it worked perfectly — so the app was fine. The real cause:
the **browser's saved-password feature** was silently replacing `admin` / `admin123` in
the login boxes with a *different* saved password (left over from another local project),
and submitting those wrong details.

**How we fixed it:** We refreshed the page (`Ctrl + Shift + R`), cleared the login boxes,
and typed the credentials in by hand instead of accepting the browser's suggestion.

**Lesson learned:** When something works in one place but fails in another, the problem is
often *outside* the program. Here it was the browser, not the app. Always check what is
actually being sent before assuming the code is broken.

---

### Problem #5 — After moving the database, the app couldn't find it

**The error:**
```
Connection to localhost:5432 refused. Check that the hostname and port are correct...
```

**What it meant to us:** We had just moved the database to door **5433** (Problem #3), but
the app was still looking for it at the old door, **5432**. So the app knocked on an empty
door and got no answer.

**How we fixed it:** We updated the app's settings to look for the database at **5433**.

**Lesson learned:** When you change one part of a system, anything that *depends* on it
must be updated too. Moving the database meant the app's address book was now out of date.

---

### Problem #6 — A setting name that was out of date

**The warning:**
```
Unrecognized configuration key "quarkus.http.cors" was provided; it will be ignored
```

**What it meant to us:** One of our configuration settings used an **old name** that the
current version of our framework no longer recognises — so it was being silently ignored.

**How we fixed it:** We renamed it to the current version: `quarkus.http.cors.enabled=true`.

**Lesson learned:** Software frameworks evolve, and setting names sometimes change between
versions. "Unrecognized key" warnings are easy to miss but mean a setting isn't actually
doing anything — worth reading the warnings, not just the errors.

---

### Problem #7 — The database and the app disagreed on how to number records (technical, but impactful)

**The error (during testing):**
```
relation "customers_seq" does not exist
```

**What it meant to us:** Every customer, offer, etc. gets a unique ID number (1, 2, 3…).
There are two common ways to generate those numbers, and our **app** was set up to use one
method while our **database** was built to use the other. They disagreed, so saving anything
failed.

**How we fixed it:** We changed the app to use the **same** numbering method the database
uses, by introducing one shared rule applied to every type of record.

**Lesson learned:** Two correct systems can still fail if they don't agree on the details.
Consistency between the app and the database matters as much as each being right on its own.

---

### Problem #8 — Tests clashing with a running app (port 8081)

**The error (while running automated tests):**
```
Port already bound: 8081: Address already in use
```

**What it meant to us:** Our automated tests start a temporary copy of the app on door
**8081** to check everything works. But another project was already using 8081, so the
tests couldn't start.

**How we fixed it:** We told the tests to grab **any free door automatically** instead of
insisting on 8081.

**Lesson learned:** Even temporary, behind-the-scenes processes need a free door. Letting
the computer pick a free one automatically avoids clashes on a busy machine.

---

## 5. The big-picture lessons

1. **"Already in use" / "port" problems were our #1 time-sink.** A computer has limited
   numbered doors; running several projects at once means carefully giving each its own.
   (This hit us four separate times — Problems #1, #3, #5, #8.)
2. **Change one thing → check everything that depends on it.** Moving the database broke
   the app's connection until we updated it too.
3. **A failing result doesn't always mean broken code.** The login "failure" was actually
   the browser's auto-fill, not the app.
4. **Read the warnings, not just the errors.** The ignored setting (Problem #6) caused no
   crash but quietly did nothing.
5. **Different parts must agree.** The app and database had to use the same record-numbering
   method (Problem #7).

---

## 6. Quick reference

| Thing                     | Where it is                              |
|---------------------------|------------------------------------------|
| The app (in your browser) | http://localhost:8090                    |
| API explorer (Swagger)    | http://localhost:8090/swagger            |
| Database                  | Running in Docker on port 5433           |
| Admin login               | `admin` / `admin123`                     |
| Advisor login             | `advisor` / `advisor123`                 |
| Start everything (Docker) | `docker compose up -d --build`           |
| Start DB only + live app  | `docker compose up -d postgres` then `./mvnw quarkus:dev` |
| Stop everything           | `docker compose down`                    |

> For the in-depth technical documentation (architecture, API endpoints, design
> decisions), see **`README.md`**.
