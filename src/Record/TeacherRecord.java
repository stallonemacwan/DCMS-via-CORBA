package Record;
/**
 * This class is used for storing various details of Teachers
 *
 */
public class TeacherRecord extends Record{

    private String address;
    private String phone;
    private String specialization;
    private String location;

    public TeacherRecord(String recID, String firstName, String lastName, String address, String phone, String special, String location){
        setRecordID(recID);
        setFirstName(firstName);
        setLastName(lastName);
        setAddress(address);
        setPhone(phone);
        setSpecialization(special);
        setLocation(location);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String toStringT(){
        String result = null;
        result = "Record ID: "+getRecordID() + "\t" +"Name: " + getFirstName() + " " + getLastName() +"\n";
        result += "Address: "+getAddress() + "\t Phone: " + getPhone() +"\n";
        result += "Specialization: "+getSpecialization() + "\t Location: " + getLocation() +"\n";
        return result;
    }
}
