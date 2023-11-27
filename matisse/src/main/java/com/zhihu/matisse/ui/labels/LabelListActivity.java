package com.zhihu.matisse.ui.labels;

import android.net.Uri;

import androidx.recyclerview.widget.GridLayoutManager;

import com.engineer.ai.util.ImageLabelHelper;
import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.ui.widget.MediaGridInset;

import java.util.List;

public class LabelListActivity extends BaseLabelListActivity {

    @Override
    void setUpRecyclerView() {
        LabelListAdapter adapter = new LabelListAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        int spacing = getResources().getDimensionPixelSize(R.dimen.media_grid_spacing);
        recyclerView.addItemDecoration(new MediaGridInset(3, spacing, false));

        if (getIntent().getExtras() != null) {
            List<Uri> uriList = getIntent().getExtras().getParcelableArrayList("list");
            adapter.update(uriList);
        }

    }

    @Override
    void setupTitle() {
        String titleStr = getIntent().getStringExtra("label");
        title.setText(titleStr);
    }
}
