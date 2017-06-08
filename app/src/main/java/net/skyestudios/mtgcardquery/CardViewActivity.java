package net.skyestudios.mtgcardquery;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.skyestudios.mtgcardquery.assets.AssetDownloader;
import net.skyestudios.mtgcardquery.data.Card;
import net.skyestudios.mtgcardquery.data.Wishlist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CardViewActivity extends AppCompatActivity {

    public final static String VIEW_TYPE = "VIEW_TYPE";
    public final static String VIEW_CARD = "CARD";
    public final static String VIEW_CARD_IMAGE = "IMAGE";
    public static final String CARD = "CARD";
    TextView name;
    TextView colors;
    LinearLayout manaCost;
    TextView cmc;
    TextView types;
    TextView cardText;
    ImageView cardImageView;
    Toolbar toolbar;
    List<String> wishlistNames;
    private Intent cardViewIntent;
    private Context context;
    private Card card;
    private File wishlistFile;
    private List<Wishlist> wishlists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cardViewIntent = getIntent();
        context = getApplicationContext();

        wishlistNames = new ArrayList<>();

        wishlistFile = new File(getFilesDir(), "wishlists.bin");
        wishlists = loadWishlists();
        if (wishlists != null) {
            for (Wishlist wishlist :
                    wishlists) {
                wishlistNames.add(wishlist.getName());
            }
        } else {
            try {
                wishlistFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Bundle extras = cardViewIntent.getExtras();
        if (extras.getString(VIEW_TYPE).equals(VIEW_CARD)) {
            setContentView(R.layout.layout_card_view);
        } else if (extras.getString(VIEW_TYPE).equals(VIEW_CARD_IMAGE)) {
            setContentView(R.layout.layout_card_image_view);
        } else if ((card = (Card) extras.getSerializable(CARD)) == null) {
            throw new IllegalArgumentException();
        } else {
            throw new IllegalArgumentException();
        }
        if ((card = (Card) extras.getSerializable(CARD)) == null) {
            throw new IllegalArgumentException();
        }
        setup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card_view, menu);
        ComponentName cn = getCallingActivity();
        String s = cn.getClassName();
        return !s.equals("net.skyestudios.mtgcardquery.WishlistEditorActivity");
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_add_to_wishlist:

                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Wishlists")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setItems(wishlistNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (wishlists.get(i).contains(card)) {
                                    new AlertDialog.Builder(CardViewActivity.this)
                                            .setTitle("Error")
                                            .setMessage("Card is already in selected wishlist")
                                            .setNegativeButton("Dismiss", null)
                                            .show();
                                } else {
                                    wishlists.get(i).addCard(card);
                                    saveWishlists(wishlists);
                                }

                                dialogInterface.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .create();

                if (wishlistNames.size() == 0) {
                    alertDialog.setMessage("Uh, oh! There are no wishlists! Please go to the wishlist submenu in the navigation panel.");
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Dismiss", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                }
                alertDialog.show();
                break;
        }
        return true;
    }

    private void setup() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(card.getName());
        setSupportActionBar(toolbar);

        name = (TextView) this.findViewById(R.id.name_textView);
        name.setText(card.getName());
        colors = (TextView) this.findViewById(R.id.colors_textView);
        colors.setText(card.getColors());
        manaCost = (LinearLayout) this.findViewById(R.id.manaCost_container);
        convertMana(card.getManaCost(), manaCost);
        cmc = (TextView) this.findViewById(R.id.cmc_textView);
        cmc.setText(String.format(Locale.US, "%.1f", card.getCmc()));
        types = (TextView) this.findViewById(R.id.types_textView);
        types.setText(card.getType());
        cardText = (TextView) this.findViewById(R.id.cardText_textView);
        cardText.setText(card.getText());
        cardImageView = new ImageView(this);
        cardImageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        cardImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        cardImageView.setAdjustViewBounds(true);
        new AssetDownloader(cardImageView).execute(card.getName());
        ((LinearLayout) findViewById(R.id.card_details)).addView(cardImageView);
    }

    private void convertMana(String manaCost, LinearLayout manaCostHolder) {
        manaCost = manaCost.replaceAll("\\}", "\\},");
        String[] manaCostTokens = manaCost.split(",");

        for (String manaCostToken :
                manaCostTokens) {
            ImageView manaImageView = new ImageView(getBaseContext());

            manaImageView.setPadding(0, 0, ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getBaseContext().getResources().getDisplayMetrics())), 0);

            switch (manaCostToken) {
                case "{0}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_0));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_0));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{1}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_1));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_1));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{2}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_2));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_2));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{3}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_3));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_3));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{4}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_4));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_4));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{5}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_5));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_5));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{6}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_6));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_6));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{7}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_7));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_7));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{8}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_8));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_8));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{9}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_9));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_9));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{10}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_10));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_10));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{11}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_11));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_11));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{12}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_12));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_12));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{13}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_13));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_13));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{14}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_14));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_14));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{15}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_15));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_15));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{16}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_16));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_16));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{17}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_17));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_17));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{18}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_18));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_18));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{19}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_19));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_19));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{20}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_20));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_20));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{X}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_x));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_x));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{R}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_r));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_r));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{G}":
                    if (manaCost.contains("{G}")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_g));
                        } else {
                            manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_g));
                        }
                        manaCostHolder.addView(manaImageView);
                    }
                    break;
                case "{B}":
                    if (manaCost.contains("{B}")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_b));
                        } else {
                            manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_b));
                        }
                        manaCostHolder.addView(manaImageView);
                    }
                    break;
                case "{U}":
                    if (manaCost.contains("{U}")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_u));
                        } else {
                            manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_u));
                        }
                        manaCostHolder.addView(manaImageView);
                    }
                    break;
                case "{W}":
                    if (manaCost.contains("{W}")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_w));
                        } else {
                            manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_w));
                        }
                        manaCostHolder.addView(manaImageView);
                    }
                    break;

                case "{2/B}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_2b));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_2b));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{2/G}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_2g));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_2g));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{2/U}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_2u));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_2u));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{2/R}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_2r));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_2r));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;
                case "{2/W}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_2w));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_2w));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;

                case "{B/G}":
                    break;
                case "{B/U}":
                    break;
                case "{B/R}":
                    break;
                case "{B/W}":
                    break;

                case "{G/B}":
                    break;
                case "{G/U}":
                    break;
                case "{G/R}":
                    break;
                case "{G/W}":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        manaImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.ic_gw));
                    } else {
                        manaImageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ic_gw));
                    }
                    manaCostHolder.addView(manaImageView);
                    break;

                case "{U/G}":
                    break;
                case "{U/B}":
                    break;
                case "{U/R}":
                    break;
                case "{U/W}":
                    break;
                default:
                    break;
            }
        }
    }
}
