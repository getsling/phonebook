package com.gangverk.mannvit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.gangverk.mannvit.PhonebookActivity;
import com.gangverk.mannvit.R;
import com.gangverk.mannvit.database.ContactsProvider;
import com.gangverk.mannvit.imageprocessing.ImageCache;
import com.gangverk.mannvit.imageprocessing.ImageCache.ImageCacheParams;
import com.gangverk.mannvit.imageprocessing.ImageWorker;
import com.gangverk.mannvit.imageprocessing.ProcessingUtils;

public class AlphabetizedAdapter extends SimpleCursorAdapter implements SectionIndexer, Filterable{

	private static final int TYPE_HEADER = 1;
	private static final int TYPE_NORMAL = 0;
	private static final int TYPE_COUNT = 2;

	private ImageWorker mImageWorker;
	private View.OnClickListener callButtonListener = null;
	private Map<Integer, Integer> mapNumberPrefs = null;
	private AlphabetIndexer indexer;
	private int[] usedSectionNumbers;
	private Map<Integer, Integer> sectionToOffset;
	private Map<Integer, Integer> sectionToPosition;
	private final LayoutInflater mLayoutInflater;
	private Context context;

	public AlphabetizedAdapter(Context context, int layout, Cursor c,String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		ImageCacheParams params = new ImageCacheParams("phonebook");
		params.bitmapHeight = 100;
		params.bitmapWidth = params.bitmapHeight;
		params.memCacheSize = ProcessingUtils.getMemoryClass(context)/8;
		com.gangverk.mannvit.imageprocessing.ImageCache cache = new ImageCache(context, params);
		mImageWorker = new ImageWorker(context, cache, R.drawable.icon, true);
		this.context = context;
		mLayoutInflater = LayoutInflater.from(context);
		indexer = new AlphabetIndexer(c, c.getColumnIndexOrThrow(ContactsProvider.NAME), "ABCDEFGHIJKLMNOPQRSTUVWXYZÞÆÖ ");
		sectionToPosition = new TreeMap<Integer, Integer>(); //use a TreeMap because we are going to iterate over its keys in sorted order
		sectionToOffset = new HashMap<Integer, Integer>();

		final int count = super.getCount();

		int i;
		//temporarily have a map alphabet section to first index it appears
		//(this map is going to be doing somethine else later)
		for (i = count - 1 ; i >= 0; i--){
			sectionToPosition.put(indexer.getSectionForPosition(i), i);
		}

		i = 0;
		usedSectionNumbers = new int[sectionToPosition.keySet().size()];

		//note that for each section that appears before a position, we must offset our
		//indices by 1, to make room for an alphabetical header in our list
		for (Integer section : sectionToPosition.keySet()){
			sectionToOffset.put(section, i);
			usedSectionNumbers[i] = section;
			i++;
		}

		//use offset to map the alphabet sections to their actual indicies in the list
		for(Integer section: sectionToPosition.keySet()){
			sectionToPosition.put(section, sectionToPosition.get(section) + sectionToOffset.get(section));
		}
	}

	@Override
	public int getCount() {
		if (super.getCount() != 0){
			//sometimes your data set gets invalidated. In this case getCount()
			//should return 0 and not our adjusted count for the headers.
			//The only way to know if data is invalidated is to check if
			//super.getCount() is 0.
			return super.getCount() + usedSectionNumbers.length;
		}

		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (getItemViewType(position) == TYPE_NORMAL){//we define this function in the full code later
			//if the list item is not a header, then we fetch the data set item with the same position
			//off-setted by the number of headers that appear before the item in the list
			return super.getItem(position - sectionToOffset.get(getSectionForPosition(position)) - 1);
		}

		return null;
	}

	@Override
	public long getItemId(int position) {
		if (mDataValid && mCursor != null) {
			if(getItemViewType(position)==TYPE_NORMAL) {
				return super.getItemId(position - sectionToOffset.get(getSectionForPosition(position)) - 1);
			}
		}
		return 0;
	}

	@Override
	public int getPositionForSection(int section) {
		if (! sectionToOffset.containsKey(section)){ 
			//This is only the case when the FastScroller is scrolling,
			//and so this section doesn't appear in our data set. The implementation
			//of Fastscroller requires that missing sections have the same index as the
			//beginning of the next non-missing section (or the end of the the list if 
			//if the rest of the sections are missing).
			//So, in pictorial example, the sections D and E would appear at position 9
			//and G to Z appear in position 11.
			int i = 0;
			int maxLength = usedSectionNumbers.length;

			//linear scan over the sections (constant number of these) that appear in the 
			//data set to find the first used section that is greater than the given section, so in the
			//example D and E correspond to F
			while (i < maxLength && section > usedSectionNumbers[i]){
				i++;
			}
			if (i == maxLength) return getCount(); //the given section is past all our data

			return indexer.getPositionForSection(usedSectionNumbers[i]) + sectionToOffset.get(usedSectionNumbers[i]);
		}

		return indexer.getPositionForSection(section) + sectionToOffset.get(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		int i = 0;		
		int maxLength = usedSectionNumbers.length;

		//linear scan over the used alphabetical sections' positions
		//to find where the given section fits in
		while (i < maxLength && position >= sectionToPosition.get(usedSectionNumbers[i])){
			i++;
		}
		return usedSectionNumbers[i-1];
	}

	@Override
	public Object[] getSections() {
		return indexer.getSections();
	}
	//nothing much to this: headers have positions that the sectionIndexer manages.
	@Override
	public int getItemViewType(int position) {
		if (position == getPositionForSection(getSectionForPosition(position))){
			return TYPE_HEADER;
		} return TYPE_NORMAL;
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_COUNT;
	}

	//return the header view, if it's in a section header position
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final int type = getItemViewType(position);
		if (type == TYPE_HEADER){
			if (convertView == null){
				convertView = mLayoutInflater.inflate (R.layout.alphabet_header, parent, false);
			}
			((TextView)convertView.findViewById(R.id.header)).setText((String)getSections()[getSectionForPosition(position)]);
			return convertView;
		}
		return super.getView(position - sectionToOffset.get(getSectionForPosition(position)) - 1, convertView, parent); 
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);
		int id = cursor.getInt(cursor.getColumnIndex(ContactsProvider._ID));
		String phone = cursor.getString(cursor.getColumnIndex(ContactsProvider.PHONE));
		TextView tv_smallText = (TextView)view.findViewById(R.id.textSmall1);

		//AssetManager assetManager = context.getAssets();
		//InputStream istr = null;
		ImageView iv_smallProfilePic = (ImageView)view.findViewById(R.id.smallProfilePic);
		mImageWorker.loadImage(cursor.getString(cursor.getColumnIndex(ContactsProvider.IMAGE_URL)), iv_smallProfilePic);
		/*try {
			istr = assetManager.open("profile/img_"+id+".jpg");
			Bitmap bitmap = BitmapFactory.decodeStream(istr);
			iv_smallProfilePic.setImageBitmap(bitmap);
		} catch (IOException e) {
			iv_smallProfilePic.setImageResource(R.drawable.icon);
		}*/

		int checkText = tv_smallText.getText().length();
		boolean prefersPhone = false;
		prefersPhone = PhonebookActivity.checkPhone(id,mapNumberPrefs,PhonebookActivity.NUMBER_PREFERENCE_PHONE);

		if(checkText == 0 || prefersPhone) {
			tv_smallText.setText(phone);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);		
		DontPressWithParentImgButton btnCall = (DontPressWithParentImgButton)view.findViewById(R.id.btnCall);
		btnCall.setOnClickListener(callButtonListener);
		return view;
	}

	public void setCallButtonListener(OnClickListener callButtonListener) {
		this.callButtonListener = callButtonListener;
	}

	public void updateNumberPreferences(Map<Integer,Integer> mapNumberPrefs) {
		this.mapNumberPrefs  = mapNumberPrefs;

	}
	@Override
	public Cursor swapCursor(Cursor c) {
		Cursor oldCursor = super.swapCursor(c);
		if(indexer != null)
			indexer.setCursor(c);
		return oldCursor;
	}

	//these two methods just disable the headers
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		if (getItemViewType(position) == TYPE_HEADER){
			return false;
		}
		return true;
	}

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		if (getFilterQueryProvider() != null) { return getFilterQueryProvider().runQuery(constraint); }

		Uri searchContact = Uri.parse("content://com.gangverk.mannvit.Contacts/contacts");
		String querySearch = (constraint == null) ? null : constraint + "%";
		String selection = (constraint == null) ? null : ContactsProvider.NAME + " LIKE ?";
		Cursor mCursor = context.getContentResolver().query(searchContact, null,selection, new String[]{querySearch}, null);
		indexer.setCursor(mCursor);
		sectionToPosition = new TreeMap<Integer, Integer>(); //use a TreeMap because we are going to iterate over its keys in sorted order
		sectionToOffset = new HashMap<Integer, Integer>();
		int count = 0;
		if (mCursor != null) {
			count = mCursor.getCount();
		} else {
			count = 0;
		}
		int i;
		//temporarily have a map alphabet section to first index it appears
		//(this map is going to be doing somethine else later)
		for (i = count - 1 ; i >= 0; i--){
			sectionToPosition.put(indexer.getSectionForPosition(i), i);
		}

		i = 0;
		usedSectionNumbers = new int[sectionToPosition.keySet().size()];

		//note that for each section that appears before a position, we must offset our
		//indices by 1, to make room for an alphabetical header in our list
		for (Integer section : sectionToPosition.keySet()){
			sectionToOffset.put(section, i);
			usedSectionNumbers[i] = section;
			i++;
		}

		//use offset to map the alphabet sections to their actual indicies in the list
		for(Integer section: sectionToPosition.keySet()){
			sectionToPosition.put(section, sectionToPosition.get(section) + sectionToOffset.get(section));
		}
		return mCursor;
	}

}
