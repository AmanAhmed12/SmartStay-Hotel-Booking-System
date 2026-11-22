package smartstay.model;

public class Room {

    private final String roomId;
    private RoomType roomType;
    private BedType bedType;
    private RoomStatus status;

    public Room(String roomId, RoomType roomType, BedType bedType, RoomStatus status) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.bedType = bedType;
        this.status = status;
    }

    public String getRoomId() {
        return roomId;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public BedType getBedType() {
        return bedType;
    }

    public void setBedType(BedType bedType) {
        this.bedType = bedType;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public double getDailyRate() {
        return roomType.getDailyRate();
    }
}
