package example.com.meetingselectdemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import example.com.meetingselectdemo.testdata.PeopleData;

public class MainActivity extends Activity {


    private MeetingMapView seatTableView;
    private Button seatButton;
    private int[][] seat = {
            {0, 0, 0, 1, 0, 0, 0},
            {1, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 1},
            {0, 0, 0, 1, 0, 0, 0}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seatTableView = (MeetingMapView) findViewById(R.id.seatView);
        seatTableView.setRowAndColumn(seat.length, seat[0].length);
        seatTableView.setData(new PeopleData().people);
        seatTableView.setSeatChecker(new MeetingMapView.SeatChecker() {

            @Override
            public boolean isValidSeat(int row, int column) {
                if (seat[row][column]==0){
                    return false;
                }
                return true;
            }

            @Override
            public boolean isSelect(int row, int column) {
                return false;
            }

            @Override
            public void checked(int row, int column) {

            }

            @Override
            public void unCheck(int row, int column) {

            }

        });
    }
}
