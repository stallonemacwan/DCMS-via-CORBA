package Record;
/**
 * This class is used for storing various details of Students
 */
public class StudentRecord extends Record{
    private String coursesRegistered;
    private String status;
    private String statusDate;

    public StudentRecord(String recID, String firstName, String lastName, String courseReg, String status, String statusDate){
        setRecordID(recID);
        setFirstName(firstName);
        setLastName(lastName);
        setCoursesRegistered(courseReg);
        setStatus(status);
        setStatusDate(statusDate);
    }

    public String getCoursesRegistered() {
        return coursesRegistered;
    }

    public void setCoursesRegistered(String coursesRegistered) {
        this.coursesRegistered = coursesRegistered;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(String statusDate) {
        this.statusDate = statusDate;
    }

    public String toStringS(){

        String result = null;
        result = "Record ID: "+ getRecordID() + "\t" +"Name: " + getFirstName() + " " + getLastName() +"\n";
        result += "Courses Registered: " + getCoursesRegistered() + "\t Status: " + getStatus() +"\n";
        result += "Status Date: "+ getStatusDate() + "\n";

        return result;
    }

}
