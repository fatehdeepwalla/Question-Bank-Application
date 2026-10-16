# Question Bank Application

A **GATE Computer Science (CSE) previous-year question organizer** built to make exam preparation more structured. I created this project while preparing for GATE so I could keep questions in one place, classify them by topic, and quickly revisit solved, repeated, and important questions.

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Template%20Engine-Thymeleaf-005F0F?logo=thymeleaf&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)

## Why I Built It

During GATE preparation, I wanted a single, organized place to work through previous-year questions instead of managing question images and revision notes as a scattered collection. This application combines image-based questions with searchable metadata, revision tags, and editing tools.

## Features

- **Organized question library:** Displays questions using their associated images, with metadata for year, subject, topic, question type, answer, and tags.
- **Flexible filtering:** Narrow the collection by GATE year, subject, topic, and tag. Tag filters support including or excluding questions with a particular tag.
- **Revision tags:** Mark questions as **Solved**, **Repeated**, **Important**, or **Very Important** to help prioritize revision.
- **Sorting:** Sort questions by question ID or year, in ascending or descending order.
- **Question editing:** Update a question's year, subject/topic classification, question type, answer information, and tags from the web interface.
- **Progress and data-quality statistics:** View counts for tagged and classified questions, as well as questions with missing metadata.
- **Pagination:** Browse the filtered results in smaller pages while retaining the selected filters and sorting options.
- **Persistent local storage:** Question metadata is stored in individual JSON files. Changes made through the editor are saved to the corresponding file, so they remain available after restart.

## Tech Stack

| Technology | Use |
| --- | --- |
| Java 17 | Application language |
| Spring Boot 4.1.1 | Application setup and runtime |
| Spring MVC | Request handling and controllers |
| Thymeleaf | Server-rendered web pages |
| Jackson | Reading and writing question JSON |
| Maven Wrapper | Build and run commands |
| JSON files and image assets | Local question data and question images |

## How It Works

1. Each question has a JSON metadata file in \`JSON_Files/\`, named \`question-<id>.json\`.
2. The associated question image is stored in \`Question_Images/\`.
3. When the application starts, it reads the JSON records into the question catalog.
4. Spring MVC applies the requested filters and sorting, then renders the results with Thymeleaf.
5. When a question is edited, the application validates the submitted metadata and writes the updated record to its JSON file.

The project uses **file-based storage**, so a separate database setup is not required.

## Getting Started

### Prerequisites

- JDK 17 or later
- Git

### Run the Application

Clone the repository and move into the project directory:

\`\`\`bash
git clone https://github.com/fatehdeepwalla/Question-Bank-Application.git
cd Question-Bank-Application
\`\`\`

On **macOS/Linux**, start the application with the Maven Wrapper:

\`\`\`bash
./mvnw spring-boot:run
\`\`\`

On **Windows**, run:

\`\`\`powershell
.\mvnw.cmd spring-boot:run
\`\`\`

Once startup is complete, open [http://localhost:8080/questions](http://localhost:8080/questions) in your browser.

### Run Tests

macOS/Linux:

\`\`\`bash
./mvnw test
\`\`\`

Windows:

\`\`\`powershell
.\mvnw.cmd test
\`\`\`

## Project Structure

\`\`\`text
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
\`\`\`

## Question Data

Question metadata includes an ID, image path, year, subject, topic, question type, answer, and tags. The application supports these GATE-style answer formats:

- **MCQ** — one correct option
- **MSQ** — multiple correct options
- **NAT** — a numeric answer represented by a minimum and maximum value

Example metadata shape:

\`\`\`json
{
  "id": 1,
  "imagePath": "Question_Images/example.png",
  "year": 2021,
  "subject": "Discrete_Mathematics",
  "topic": "Relation",
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
\`\`\`

This is an example of the data format; actual records and image paths are in the repository.

## Future Scope

Potential extensions include user accounts, cloud/database-backed storage, and richer revision or practice modes. The current version focuses on keeping the local GATE CSE question collection organized, editable, and easy to review.
