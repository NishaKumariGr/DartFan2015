package dartmouth.cs.ploomis.dartfan;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Dialog for showing a date and time picker.
 */

public class DateTimeDialogFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final String DIALOG_ID = "dialog_id";
    public static final int ID_DATE_INPUT = 0;
    public static final int ID_TIME_INPUT = 1;

    public static DateTimeDialogFragment newInstance(int dialog_id) {
        DateTimeDialogFragment dialogFragment = new DateTimeDialogFragment();
        Bundle args = new Bundle();
        args.putInt(DIALOG_ID, dialog_id);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    /** Create a date picker dialog. Use current date as default. */
    private DatePickerDialog createDatePickerDialog() {
        final Calendar dateCal = Calendar.getInstance();
        int year = dateCal.get(Calendar.YEAR);
        int month = dateCal.get(Calendar.MONTH);
        int day = dateCal.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    /** Create a time picker dialog. Use current time as default */
    private TimePickerDialog createTimePickerDialog() {
        final Calendar timeCal = Calendar.getInstance();
        int hour = timeCal.get(Calendar.HOUR_OF_DAY);
        int minute = timeCal.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int dialog_id = getArguments().getInt(DIALOG_ID);

        switch (dialog_id) {
            case ID_DATE_INPUT:
                return createDatePickerDialog();
            case ID_TIME_INPUT:
                return createTimePickerDialog();
            default:
                return null;
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        ((NewEventActivity) getActivity()).onDateSet(year, monthOfYear, dayOfMonth);

        // display time picker after date is set
        DateTimeDialogFragment fragment = DateTimeDialogFragment.newInstance(ID_TIME_INPUT);
        fragment.show(getFragmentManager(), "Choose reminder time");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        ((NewEventActivity) getActivity()).onTimeSet(hourOfDay, minute);
    }
}
