package parrtim.applicationfundamentals.fragments;

import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.Objects;

import parrtim.applicationfundamentals.adapters.InboxListAdapter;
import parrtim.applicationfundamentals.R;
import parrtim.applicationfundamentals.providers.RecentSearchSuggestionsProvider;
import parrtim.applicationfundamentals.helper.SMSUtil;

public class InboxFragment extends ListFragment implements AdapterView.OnItemClickListener {

    InboxListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        adapter = new InboxListAdapter(getContext(), SMSUtil.getSMSInbox(getContext()));
        return inflater.inflate(R.layout.fragment_inbox_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();

        this.getFragmentManager().beginTransaction()
                .replace(R.id.frame1, new SMSMessageParentFragment(), null)
                .addToBackStack(null)
                .commit();
    }

    public void SetItem(int position) {
        getListView().setSelection(position);
    }

    public void Filter(String filterText)
    {
        adapter.getFilter().filter(filterText);
    }
}