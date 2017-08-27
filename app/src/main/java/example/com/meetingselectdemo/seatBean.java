package example.com.meetingselectdemo;

/**
     * Created by Siqi on 2017/8/23.
 */

public class SeatBean {
    public int column;
    public int row;
    public int userId;
    public String userName;
    public boolean setSeat;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSetSeat() {
        return setSeat;
    }

    public void setSetSeat(boolean setSeat) {
        this.setSeat = setSeat;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

}
