package com.tehflatch.liquid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import static com.tehflatch.liquid.MainActivity.appContext;
import static com.tehflatch.liquid.MainActivity.areYouSureString;
import static com.tehflatch.liquid.MainActivity.cigDeleteString;
import static com.tehflatch.liquid.MainActivity.noString;
import static com.tehflatch.liquid.MainActivity.yesString;

class CigaretteHistoryViewHolder extends RecyclerView.ViewHolder {

    private TextView date;
    private TextView price;
    private TextView number;
    private TextView brand;
    private TextView key;

    public CigaretteHistoryViewHolder(final View v) {
        super(v);

        date = v.findViewById(R.id.date);
        price = v.findViewById(R.id.price);
        number = v.findViewById(R.id.number);
        brand = v.findViewById(R.id.brand);
        key = v.findViewById(R.id.key);

        TextView deleteButton = v.findViewById(R.id.deleteButton);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage(cigDeleteString)
                        .setTitle(areYouSureString);
                builder.setPositiveButton(yesString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NewCigarette.DeleteCigarette(key.getText().toString());
                        Snackbar.make(v, appContext.getString(R.string.success_delete), Snackbar.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(noString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog confirmDelete = builder.create();
                confirmDelete.show();

            }
        });

    }
    public void bindData(final CigaretteHistoryModel viewModel) {
        date.setText(viewModel.getDate());
        number.setText(viewModel.getId());
        brand.setText(viewModel.getBrand());
        price.setText(viewModel.getPrice());
        key.setText(viewModel.getKey());
    }
}
