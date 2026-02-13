# Data Folder

This folder contains CSV roster files for course management.

## ⚠️ Privacy Notice
This folder is excluded from version control via `.gitignore` as it contains sensitive student information.

## 📁 Expected File Format

Place your CSV roster files here following this naming convention:

```
CRN-SemesterTerm Year-SUBJECTnumberSECTION.csv
```

### Examples:
- `83129-Fall 2025-CSCI1302A.csv`
- `84201-Spring 2025-MATH2008B.csv`
- `85122-Summer 2025-ENGL1101C.csv`

## 📋 CSV Structure

Each CSV file should contain the following columns:

| Column | Description | Required | Example |
|--------|-------------|----------|---------|
| Last Name | Student's last name | Yes | Smith |
| First Name | Student's first name | Yes | John |
| Middle Name | Student's middle name | No | Allen |
| Preferred First Name | Preferred name | No | Johnny |
| Email | Student email | Yes | jsmith@example.edu |
| Eagle ID | Student ID number | Yes | 901234567 |
| Major | Student's major | Yes | Computer Science |
| Campus | Campus location | Yes | Statesboro Campus |
| First Day of Isolation/Quarantine | Date (dd-MMM-yy) | No | 15-Jan-25 |
| Last Day of Isolation/Quarantine | Date (dd-MMM-yy) | No | 20-Jan-25 |
| Registered Late | Late registration date | No | 10-Jan-25 |
| Attendance Status | Y or N | No | Y |

## 🔒 Security Best Practices

1. **Never commit real student data to version control**
2. Keep this folder in `.gitignore`
3. Ensure proper file permissions on your local system
4. Use encrypted backups for sensitive data
5. Consider using environment-specific data folders

## 🧪 Testing

For testing purposes, create sample/dummy data files with fictional student information.

Example test file: `99999-Fall 2025-TEST1000A.csv`
```csv
Last Name,First Name,Middle Name,Preferred First Name,Email,Eagle ID,Major,Campus,First Day of Isolation/Quarantine Modification,Last Day of Isolation/Quarantine Modification,Registered Late,Attendance Status
Doe,John,M,Johnny,jdoe@example.edu,900000001,Computer Science,Statesboro Campus,null,null,null,Y
Smith,Jane,A,Jane,jsmith@example.edu,900000002,Mathematics,Statesboro Campus,null,null,15-Jan-25,Y
```

## 📍 File Location

The default data folder path is configured in `RosterLoader.java`:
```java
final static File DATA_FOLDER_PATH = new File("C:\\Users\\jfarara\\Documents\\Github\\ILEP\\Data");
```

Update this path to match your local system configuration.