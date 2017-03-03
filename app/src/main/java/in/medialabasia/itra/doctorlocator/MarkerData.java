package in.medialabasia.itra.doctorlocator;

/**
 * Created by ITRA on 01-03-2017.
 */
public class MarkerData {
    private String name;
    private String specialization;
    private String email;
    private String phoneNumber;
    private String availableBeds;


    public MarkerData(String name, String specialization, String email, String phoneNumber,  String availableBeds) {
        this.name = name;
        this.specialization = specialization;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.availableBeds = availableBeds;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvailableBeds() {
        return availableBeds;
    }

    public void setAvailableBeds(String availableBeds) {
        this.availableBeds = availableBeds;
    }
}
