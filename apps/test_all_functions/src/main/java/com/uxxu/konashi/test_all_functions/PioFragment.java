package com.uxxu.konashi.test_all_functions;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.uxxu.konashi.lib.Konashi;
import com.uxxu.konashi.lib.KonashiManager;
import com.uxxu.konashi.lib.KonashiObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kiryu on 7/27/15.
 */
public final class PioFragment extends Fragment {

    public static final String TITLE = "PIO";

    private final KonashiManager mKonashiManager = Konashi.getManager();

    private TableLayout mTableLayout;
    private List<PioTableRow> rows = new ArrayList<>();

    private KonashiObserver mInputObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(TITLE);

        mInputObserver = new KonashiObserver(getActivity()) {
            @Override
            public void onUpdatePioInput(byte value) {
                rows.get(0).setInputValue(value);
            }
        };
        mKonashiManager.addObserver(mInputObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pio, container, false);
        mTableLayout = (TableLayout) view.findViewById(R.id.tableLayout);
        mTableLayout.addView(new HeaderTableRow(getActivity()));
        for (int pinNumber : Utils.PIO_PINS) {
            PioTableRow row = PioTableRow.createWithPinNumber(getActivity(), pinNumber);
            mTableLayout.addView(row);
            rows.add(row);
        }
        return view;
    }

    @Override
    public void onDestroy() {
        if (mKonashiManager.isReady()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int pinNumber : Utils.PIO_PINS) {
                        mKonashiManager.pinMode(pinNumber, Konashi.INPUT);
                    }
                }
            }).start();
        }
        mKonashiManager.removeObserver(mInputObserver);
        super.onDestroy();
    }

    public static final class HeaderTableRow extends TableRow {

        public HeaderTableRow(Context context) {
            super(context);

            addRow("PIN", 1);
            addRow("Mode", 2);
            addRow("Output", 2);
            addRow("Input", 1);
            addRow("Pullup", 1);
        }

        private void addRow(String text, float weight) {
            TextView textView = new TextView(getContext());
            textView.setText(text);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            addView(textView, Utils.createTableRowLayoutParamwWithWeight(weight));
        }
    }

    public static final class PioTableRow extends TableRow {

        private final TextView mPinTextView;
        private final ToggleButton mIoToggleButton;
        private final ToggleButton mOutputToggleButton;
        private final TextView mInputTextView;
        private final CheckBox mPullupCheckBox;
        private final KonashiManager mKonashiManager = Konashi.getManager();
        private int mPinNumber;

        public static PioTableRow createWithPinNumber(Context context, final int pinNumber) {
            PioTableRow row = new PioTableRow(context);
            row.setPinNumber(pinNumber);
            return row;
        }

        public PioTableRow(Context context) {
            super(context);

            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            mPinTextView = new TextView(context);
            mPinTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            addView(mPinTextView, Utils.createTableRowLayoutParamwWithWeight(1));

            mIoToggleButton = new ToggleButton(context);
            mIoToggleButton.setTextOff("INPUT");
            mIoToggleButton.setTextOn("OUTPUT");
            mIoToggleButton.setText(mIoToggleButton.getTextOff());
            mIoToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int mode = b ? Konashi.OUTPUT : Konashi.INPUT;
                    switch (mode) {
                        case Konashi.OUTPUT:
                            mOutputToggleButton.setEnabled(true);
                            mInputTextView.setEnabled(false);
                            break;
                        case Konashi.INPUT:
                            mOutputToggleButton.setEnabled(false);
                            mInputTextView.setEnabled(true);
                            break;
                    }
                    mKonashiManager.pinMode(mPinNumber, mode);
                }
            });
            addView(mIoToggleButton, Utils.createTableRowLayoutParamwWithWeight(2));

            mOutputToggleButton = new ToggleButton(context);
            mOutputToggleButton.setTextOff("LOW");
            mOutputToggleButton.setTextOn("HIGH");
            mOutputToggleButton.setText(mOutputToggleButton.getTextOff());
            mOutputToggleButton.setEnabled(false);
            mOutputToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mKonashiManager.digitalWrite(mPinNumber, b ? Konashi.HIGH : Konashi.LOW);
                }
            });
            addView(mOutputToggleButton, Utils.createTableRowLayoutParamwWithWeight(2));

            mInputTextView = new TextView(context);
            mInputTextView.setText("LOW");
            mInputTextView.setGravity(Gravity.CENTER);
            addView(mInputTextView, Utils.createTableRowLayoutParamwWithWeight(1));

            mPullupCheckBox = new CheckBox(context);
            mPullupCheckBox.setGravity(Gravity.CENTER);
            mPullupCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mKonashiManager.pinPullup(mPinNumber, b ? Konashi.PULLUP : Konashi.NO_PULLS);
                }
            });
            addView(mPullupCheckBox, Utils.createTableRowLayoutParamwWithWeight(1));
        }

        public void setPinNumber(int pinNumber) {
            this.mPinNumber = pinNumber;
            mPinTextView.setText(String.valueOf(pinNumber));
        }

        public void setInputValue(int value) {
            mInputTextView.setText(value == Konashi.HIGH ? "HIGH" : "LOW");
        }
    }
}
