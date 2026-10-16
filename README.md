# Question Bank Application
- I built this application to support my preparation for the **GATE Computer Science Examination**.
- It brings previous-year questions together in single, organized collection.
- Tag questions as **Solved**, **Repeated**, **Important**, or **Very Important**.
- Filter questions by **tags**, **year**, **subject**, or **topic** (kind of sub-topic within subject).
- Sort questions by **question ID** or **year**.
- Browse questions with pagination and edit their metadata.
- Store question metadata in JSON files for persistence.

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Template%20Engine-Thymeleaf-005F0F?logo=thymeleaf&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)

---
## Why I Built It

- Several online platforms already offer GATE previous-year question banks, allowing users to filter questions by subject and topic (a subtopic within a subject).
- One such site is [practicepaper.in](https://practicepaper.in/).  
- While preparing for GATE Computer Science, I found that working through more than 3,000 previous-year questions could take nearly two months.
- Effective preparation also requires revisiting questions during revision.
- However, repeatedly working through the entire question set can be time-consuming, especially when many questions test similar concepts.
- I wanted a way to make this revision process more efficient.
- I built this application to let me mark questions as Solved, Repeated, Important, or Very Important. This helps me track my progress, identify repetitive questions, prioritize important questions during revision.
- **The goal is to save time and make revision more focused, rather than taking a brute-force appraoch of repeatedly solving the entire question set from scratch.**
- I retained the standard filtering features by year, subject, and topic, while adding revision-focused tagging to make question practice more efficient.


---
## Features

- **Organized question library:** Displays questions using their associated images, with metadata for year, subject, topic, question type, answer, and tags.
- **Flexible filtering:** Narrow the collection by GATE year, subject, topic, and tag. Tag filters support including or excluding questions with a particular tag.
- **Revision tags:** Mark questions as **Solved**, **Repeated**, **Important**, or **Very Important** to help prioritize revision.
- **Sorting:** Sort questions by **question ID** or **year**, in ascending or descending order.
- **Question editing:** Update a question's year, subject/topic classification, question type, answer information, and tags from the web interface.
- **Progress and data-quality statistics:** View counts for tagged, as well as questions with missing metadata.
- **Pagination:** Browse the filtered results in smaller pages while retaining the selected filters and sorting options.
- **Persistent local storage:** Question metadata is stored in individual JSON files. Changes made through the editor are saved to the corresponding file, so they remain available after restarting the application.

---
## Tech Stack

  | Technology | Role |
  | --- | --- |
  | Java 17 | Main programming language |
  | Spring Boot 4.1.1 | Application framework |
  | Spring MVC | Handles HTTP requests and routes them to controllers |
  | Thymeleaf | Renders server-side HTML pages |
  | Jackson Databind | Reads and writes question metadata in JSON format |
  | Maven Wrapper | Builds and runs the application using Maven commands |

---
## How It Works

1. Each question has a JSON metadata file, named `question-<id>.json` in `JSON_Files/`.
2. The associated question image is stored in `Question_Images/`.
3. When the application starts, it reads the JSON records into the question catalog.
4. Spring MVC applies the requested filters and sorting, then renders the results with Thymeleaf.
5. When a question is edited, the application validates the submitted metadata and writes the updated record to its JSON file.
6. The project uses **file-based storage**, so a separate database setup is not required.

---
## Getting Started

**Prerequisites**
- JDK 17 or later
- maven wrapper is included in project

**Run the Application**

- Clone the repository and move into the project directory:
  
  ```bash
  git clone https://github.com/fatehdeepwalla/Question-Bank-Application.git
  cd Question-Bank-Application
  ```
  
- On **macOS/Linux**, start the application with the Maven Wrapper:
  
  ```bash
  ./mvnw spring-boot:run
  ```
  
- On **Windows**, run:
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
  
- Once startup is complete, open [http://localhost:8080/questions](http://localhost:8080/questions) in your browser.

---
## Project Structure

```text
Question-Bank-Application/
├── JSON_Files/                   # Question metadata, one JSON file per question
├── Question_Images/              # Image assets for questions
├── src/
│   ├── main/
│   │   ├── java/org/example/questionBankApplication/
│   │   │   ├── Mappers/          # JSON reading, writing, and initialization
│   │   │   ├── model/            # Questions, answers, tags, subjects, and topics
│   │   │   ├── runners/          # Optional startup runners
│   │   │   └── web/              # Services, view models, and MVC controllers
│   │   └── resources/
│   │       ├── templates/        # Thymeleaf pages and reusable fragments
│   │       ├── static/           # CSS and static assets
│   │       └── application.properties
│   └── test/                     # Automated tests
├── pom.xml
├── mvnw
└── mvnw.cmd
```

---
## Question Data
- Each question is represented by a JSON metadata file containing its ID, image path, year, subject, topic, question type, answer, and tags.
  
- **Subject and Topic Relationship** : Each topic is associated with a subject. For example, `Relation` is a topic under `Discrete_Mathematics`. The subject and topic must correspond to the application's subject-topic mapping.
  
- **Question Type and Answer Relationship**
  - The structure of the `answer` field depends on the question's `type`. The application supports three GATE-style question types:
  - **MCQ (Multiple Choice Question):** Stores a single correct option using `correctOption`.
  - **MSQ (Multiple Select Question):** Stores multiple correct options using `correctOptions`.
  - **NAT (Numerical Answer Type):** Stores an accepted numerical range using `minValue` and `maxValue`.
  - The answer data must match the question type. For example, an MCQ uses `correctOption`, whereas an MSQ uses `correctOptions`.
    
- **Example JSON Metadata**
  
  ```json
  {
    "id": 1,
    "imagePath": "Question_Images/example.png",
    "year": 2021,
    "subject": "Digital_Logic",
    "topic": "Combinational_Circuit",
    "type": "MCQ",
    "answer": {
      "correctOption": "B"
    },
    "questionTags": [
      "IMPORTANT",
      "VERY_IMPORTANT",
      "REPEATED"
    ]
  }
  ```
  
   > This example illustrates an MCQ question under the `Combinational_Circuit` topic of `Digital_Logic`. Actual question records and their corresponding images are available in the repository.


---
## Future Scope

Potential extensions include user accounts, cloud/database-backed storage, and richer revision or practice modes. The current version focuses on keeping the local GATE CSE question collection organized and editable.
