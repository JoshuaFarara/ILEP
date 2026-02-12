# ILEP - Interactive Learning and Engagement Platform

A Java-based roster management system for educational institutions, designed to manage course rosters, student information, and attendance tracking.

## 📋 Overview

ILEP (Integrated Learning and Engagement Platform) is a comprehensive roster management application that allows educators to:
- Load and manage multiple course rosters from CSV files
- Track student information and attendance
- Parse structured course filenames automatically
- Manage students across different courses, sections, and semesters

## 🚀 Features

- **Automated CSV Processing**: Automatically reads student data from properly formatted CSV files
- **Intelligent Filename Parsing**: Extracts course information from standardized filename format
- **Multiple Roster Management**: Handle multiple course sections simultaneously
- **Student Information Tracking**: Comprehensive student data including:
  - Personal information (name, email, Eagle ID)
  - Academic details (major, campus)
  - Attendance status
  - Special accommodations (isolation/quarantine dates, late registration)
- **Flexible Data Loading**: Load all rosters from a folder or select individual files

## 📁 Project Structure

```
ILEP/
├── Data/                    # CSV roster files
├── Models/                  # Core data models
│   ├── Student.java        # Student entity class
│   ├── Roster.java         # Roster container class
│   ├── RosterLoader.java   # File I/O and CSV parsing
│   └── RosterManager.java  # Roster collection management
├── FlowLogic/              # Application flow and logic
├── Tests/                  # Unit tests
└── bin/                    # Compiled classes
```

## 📝 Filename Format

CSV files must follow this naming convention:
```
CRN-SemesterTerm Year-SUBJECTnumberSECTION.csv
```

**Example**: `83129-Fall 2025-CSCI1302A.csv`

Where:
- `83129` = Course Reference Number (CRN)
- `Fall` = Semester term
- `2025` = Academic year
- `CSCI` = Course subject
- `1302` = Course number
- `A` = Section identifier

## 📊 CSV File Format

CSV files should contain the following columns in order:

1. Last Name
2. First Name
3. Middle Name
4. Preferred First Name
5. Email
6. Eagle ID
7. Major
8. Campus
9. First Day of Isolation/Quarantine Modification (optional)
10. Last Day of Isolation/Quarantine Modification (optional)
11. Registered Late (optional, format: dd-MMM-yy)
12. Attendance Status (Y/N)

**Example CSV**:
```csv
Last Name, First Name,Middle Name,Preferred First Name,Email,Eagle ID,Major,Campus,First Day of Isolation/Quarantine Modification,Last Day of Isolation/Quarantine Modification,Registered Late,Attendance Status
Farara, Joshua, Abijah , @georgiasouthern.edu, 912555555, Computer Science, Statesboro Campus, null, null, 30-Oct-25, Y
```

## 🛠️ Setup and Installation

### Prerequisites
- Java Development Kit (JDK) 11 or higher
- IDE (Eclipse, IntelliJ IDEA, or VS Code with Java extensions)

### Installation

1. Clone the repository:
```bash
git clone https://github.com/JoshuaFarara/ILEP.git
cd ILEP
```

2. Set up your data folder path in `RosterLoader.java`:
```java
final static File DATA_FOLDER_PATH = new File("path/to/your/Data");
```

3. Compile the project:
```bash
javac -d bin Models/*.java FlowLogic/*.java
```

4. Place your CSV roster files in the `Data/` directory

## 💻 Usage

### Loading All Rosters

```java
// Create manager and loader
RosterManager manager = new RosterManager();
RosterLoader loader = new RosterLoader();

// Load all rosters from data folder
loader.loadAll(manager);

// Display all loaded rosters
for (Roster roster : manager.getRosters()) {
    System.out.println(roster);
}
```

### Loading a Single Roster

```java
RosterLoader loader = new RosterLoader();
Roster roster = loader.populateRosterFromFile("83129-Fall 2025-CSCI1302A.csv");

// Access roster information
System.out.println("CRN: " + roster.getCrn());
System.out.println("Course: " + roster.getCourseName() + roster.getCourseCode());
System.out.println("Students: " + roster.getNumStudentsAdded());
```

### Accessing Student Information

```java
Roster roster = manager.getRoster("CSCI1302A");

for (Student student : roster.getStudents()) {
    System.out.println(student.getFirstName() + " " + student.getLastName());
    System.out.println("Email: " + student.getEmail());
    System.out.println("Eagle ID: " + student.getEagleID());
}
```

## 🔧 Configuration

### Data Folder Path
Update the `DATA_FOLDER_PATH` in `RosterLoader.java`:
```java
final static File DATA_FOLDER_PATH = new File("C:\\Users\\YourName\\Documents\\ILEP\\Data");
```

### Date Format
Dates in CSV files should follow the format: `dd-MMM-yy` (e.g., `30-Oct-25`)

## 🧪 Testing

Run tests from the `Tests/` directory:
```bash
java -cp bin Tests.RosterLoaderTest
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📜 License

This project is open source and available for educational purposes.

## 👤 Author

**Joshua Farara**
- GitHub: [@JoshuaFarara](https://github.com/JoshuaFarara)

## 🐛 Known Issues

- Files that don't match the expected filename format will be skipped during bulk loading
- CSV files must be properly formatted with all required columns
- Date parsing only supports the `dd-MMM-yy` format

## 📋 Roadmap

- [ ] Add support for additional file formats (Excel, JSON)
- [ ] Implement attendance tracking features
- [ ] Create GUI for roster management
- [ ] Add export functionality for reports
- [ ] Implement database integration

## ❓ FAQ

**Q: What happens if a CSV file is missing columns?**  
A: The system will skip that student entry and log an error message.

**Q: Can I use different date formats?**  
A: Currently, only `dd-MMM-yy` format is supported. You'll need to modify the `SimpleDateFormat` in `RosterLoader.java` for other formats.

**Q: How do I handle invalid filenames?**  
A: Files with invalid names will throw an `IllegalArgumentException` and be skipped during bulk loading.

## 📞 Support

For issues, questions, or suggestions, please open an issue on the [GitHub repository](https://github.com/JoshuaFarara/ILEP/issues).

---

**Note**: This project is designed for educational institutions and follows standard academic roster management practices.
