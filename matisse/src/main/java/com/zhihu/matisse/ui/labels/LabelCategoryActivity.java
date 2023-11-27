package com.zhihu.matisse.ui.labels;

import androidx.recyclerview.widget.GridLayoutManager;

import com.engineer.ai.util.ImageLabelHelper;
import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.ui.widget.MediaGridInset;

public class LabelCategoryActivity extends BaseLabelListActivity {

    @Override
    void setUpRecyclerView() {
        LabelCategoryAdapter adapter = new LabelCategoryAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        int spacing = getResources().getDimensionPixelSize(R.dimen.label_grid_spacing);
        recyclerView.addItemDecoration(new MediaGridInset(3, spacing, true));
        adapter.update(ImageLabelHelper.INSTANCE.getLabelList());
    }

    @Override
    void setupTitle() {
        title.setText("图片分类");
    }
}
