package net.skyestudios.mtgcardquery.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import net.skyestudios.mtgcardquery.R;
import net.skyestudios.mtgcardquery.WishlistEditorActivity;
import net.skyestudios.mtgcardquery.adapters.WishlistViewAdapter;
import net.skyestudios.mtgcardquery.data.Wishlist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class WishlistsFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    File wishlistFile;
    ListView list_wishlists;
    WishlistViewAdapter wishlistAdapter;
    Intent wishlistEditorIntent;

    public WishlistsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wishlistFile = new File(getActivity().getFilesDir(), "wishlists.bin");
        wishlistAdapter = new WishlistViewAdapter(getContext(), R.layout.layout_item_wishlist);
        wishlistEditorIntent = new Intent(getContext(), WishlistEditorActivity.class);

        if (!wishlistFile.exists()) {
            try {
                wishlistFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            wishlistAdapter.addAll(loadWishlists());
        }
    }

    private void saveWishlists(List<Wishlist> wishlists) {
        try {
            FileOutputStream FOS = new FileOutputStream(wishlistFile);
            ObjectOutputStream OOS = new ObjectOutputStream(FOS);
            OOS.writeObject(wishlists);
            OOS.close();
            FOS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Wishlist> loadWishlists() {
        try {
            FileInputStream FIS = new FileInputStream(wishlistFile);
            ObjectInputStream OIS = new ObjectInputStream(FIS);
            ArrayList<Wishlist> wishlists = (ArrayList<Wishlist>) OIS.readObject();
            OIS.close();
            FIS.close();
            return wishlists;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);
        view.findViewById(R.id.fab_wishlist_add).setOnClickListener(this);
        list_wishlists = (ListView) view.findViewById(R.id.wishlist_ListView);
        list_wishlists.setAdapter(wishlistAdapter);
        list_wishlists.setOnItemClickListener(this);
        list_wishlists.setOnItemLongClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_wishlist_add:
                final EditText wishlistName = new EditText(getContext());
                wishlistName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                wishlistName.setText("Wishlist" + wishlistAdapter.getWishlists().size());
                wishlistName.setSelection(wishlistName.getText().toString().length());
                AlertDialog createWishlistDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Create New Wishlist")
                        .setView(wishlistName)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (wishlistName.getText().toString().trim().equals("")) {

                                } else {
                                    Wishlist wishlist = new Wishlist(wishlistName.getText().toString().trim());
                                    if (wishlistAdapter.getWishlists().contains(wishlist)) {
                                        new AlertDialog.Builder(getContext())
                                                .setTitle("Error")
                                                .setMessage("Wishlist name already in use!")
                                                .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                })
                                                .show();
                                    } else {
                                        wishlistAdapter.add(wishlist);

                                        saveWishlists(wishlistAdapter.getWishlists());
                                        wishlistAdapter.notifyDataSetChanged();
                                    }
                                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(wishlistName.getWindowToken(), 0);
                                    dialogInterface.dismiss();
                                }
                            }
                        }).create();
                createWishlistDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(wishlistName, InputMethodManager.RESULT_SHOWN);
                    }
                });
                createWishlistDialog.show();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        wishlistEditorIntent.putExtra("wishlist_position", i);
        startActivity(wishlistEditorIntent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        wishlistAdapter.remove(wishlistAdapter.getItem(i));
        saveWishlists(wishlistAdapter.getWishlists());
        return true;
    }
}
