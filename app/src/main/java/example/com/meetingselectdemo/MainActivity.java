package example.com.meetingselectdemo;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {



    public MeetingMapView seatTableView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seatTableView = (MeetingMapView) findViewById(R.id.seatView);
        seatTableView.setRowAndColumn(20,8);
        seatTableView.setSeatChecker(new MeetingMapView.SeatChecker() {

            @Override
            public boolean isValidSeat(int row, int column) {
                if (row==2||row==1){
                    return false;
                }
                if (row==0&&(column==0||column==1||column==8||column==9)){
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
