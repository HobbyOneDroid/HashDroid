/* FileActivity.java -- 
   Copyright (C) 2010 Christophe Bouyer (Hobby One)

This file is part of Hash Droid.

Hash Droid is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Hash Droid is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Hash Droid. If not, see <http://www.gnu.org/licenses/>.
 */

package com.hobbyone.HashDroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.OpenableColumns;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileActivity extends Activity implements Runnable {
	private Button mSelectFileButton = null;
	private CheckBox mCheckBox = null;
	private Button mGenerateButton = null;
	private Button mCopyButton = null;
	private Spinner mSpinner = null;
	private TextView mResultTV = null;
	private String msFileSize = "";
	private String msHash = "";
	private String[] mFunctions;
	private ClipboardManager mClipboard = null;
	private final int SELECT_FILE_REQUEST = 0;
	private HashFunctionOperator mHashOpe = null;
	private ProgressDialog mProgressDialog = null;
	private int miItePos = -1;
	private Uri mSelectedFileUri=null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file);

		mSelectFileButton = (Button) findViewById(R.id.SelectFileButton);
		mGenerateButton = (Button) findViewById(R.id.GenerateButton);
		mSpinner = (Spinner) findViewById(R.id.spinner);
		mResultTV = (TextView) findViewById(R.id.label_result);
		mCopyButton = (Button) findViewById(R.id.CopyButton);
		mClipboard = (ClipboardManager) getSystemService("clipboard");
		mFunctions = getResources().getStringArray(R.array.Algo_Array);
		mCheckBox = (CheckBox) findViewById(R.id.UpperCaseCB);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.Algo_Array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(adapter);
		mSpinner.setSelection(5); // MD5 by default

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView,
					View selectedItemView, int position, long id) {
				// your code here
				// Hide the copy button
				if (!msHash.equals(""))
					mCopyButton.setVisibility(View.INVISIBLE);
				// Clean the result text view
				if (mResultTV != null)
					mResultTV.setText("");
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// your code here
			}
		});

		mSelectFileButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent openExplorerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
					if(null != openExplorerIntent) {
						openExplorerIntent.addCategory(Intent.CATEGORY_OPENABLE);
						openExplorerIntent.setType("*/*");
						startActivityForResult(Intent.createChooser(openExplorerIntent, "Select a file"), SELECT_FILE_REQUEST);
					}
				} catch (ActivityNotFoundException e) {
				}
			}
		});

		mGenerateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Perform action on clicks
				if (null != mSelectedFileUri) {
					miItePos = mSpinner.getSelectedItemPosition();
					File fileToHash = new File(mSelectedFileUri.getPath());
					if (fileToHash != null)
						ComputeAndDisplayHash();
					else {
						String sWrongFile = getString(R.string.wrong_file);
						if (mResultTV != null)
							mResultTV.setText(sWrongFile);
						if (mCopyButton != null)
							mCopyButton.setVisibility(View.INVISIBLE);
					}
				}
			}
		});

		mCopyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Perform action on clicks
				if (mClipboard != null) {
					mClipboard.setText(msHash);
					String sCopied = getString(R.string.copied);
					Toast.makeText(FileActivity.this, sCopied,
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		mCheckBox.setChecked(false); // lower case by default
		mCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Perform action on clicks
				if (!msHash.equals("")) {
					// A hash value has already been calculated,
					// just convert it to lower or upper case
					String OldHash = msHash;
					if (mCheckBox.isChecked()) {
						msHash = OldHash.toUpperCase();
					} else {
						msHash = OldHash.toLowerCase();
					}
					if (mResultTV != null) {
						String sResult = mResultTV.getText().toString();
						sResult = sResult.replaceAll(OldHash, msHash);
						mResultTV.setText(sResult);
					}
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_FILE_REQUEST && resultCode == RESULT_OK) {
			if (data != null) {
				mSelectedFileUri = data.getData(); //The uri with the location of the file
				if(null != mSelectedFileUri) {
					Cursor cursor = getContentResolver().query(mSelectedFileUri, null, null, null, null);
					if (cursor != null && cursor.moveToFirst()) {
						String ret = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
						mSelectFileButton.setText(ret);
					}
				}
			}
		}
	}

	private void ComputeAndDisplayHash() {
		if (mHashOpe == null)
			mHashOpe = new HashFunctionOperator();
		String sAlgo = "";
		if (miItePos == 0)
			sAlgo = "Adler-32";
		else if (miItePos == 1)
			sAlgo = "CRC-32";
		else if (miItePos == 2)
			sAlgo = "Haval";
		else if (miItePos == 3)
			sAlgo = "md2";
		else if (miItePos == 4)
			sAlgo = "md4";
		else if (miItePos == 5)
			sAlgo = "md5";
		else if (miItePos == 6)
			sAlgo = "ripemd-128";
		else if (miItePos == 7)
			sAlgo = "ripemd-160";
		else if (miItePos == 8)
			sAlgo = "sha-1";
		else if (miItePos == 9)
			sAlgo = "sha-256";
		else if (miItePos == 10)
			sAlgo = "sha-384";
		else if (miItePos == 11)
			sAlgo = "sha-512";
		else if (miItePos == 12)
			sAlgo = "tiger";
		else if (miItePos == 13)
			sAlgo = "whirlpool";
		mHashOpe.SetAlgorithm(sAlgo);

		String sCalculating = getString(R.string.Calculating);
		mProgressDialog = ProgressDialog.show(FileActivity.this, "",
				sCalculating, true);

		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	// Call when the thread is started
	public void run() {
		msHash = "";
		msFileSize = "";

		if(null != mSelectedFileUri) {
			if (mHashOpe != null) {
				InputStream inputStream = null;
				try {
					inputStream = getContentResolver().openInputStream(mSelectedFileUri);

				} catch (FileNotFoundException e1) {
				}
				if (null != inputStream) {
					msHash = mHashOpe.FileToHash(inputStream);
				}
			}

			Cursor cursor = getContentResolver().query(mSelectedFileUri, null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
				msFileSize = FileSizeDisplay(cursor.getLong(sizeIndex), false);
			}
		}
		handler.sendEmptyMessage(0);
	}

	private String FileSizeDisplay(long lbytes, boolean bSI) {
		int unit = bSI ? 1000 : 1024;
		if (lbytes < unit)
			return lbytes + " B";
		int exp = (int) (Math.log(lbytes) / Math.log(unit));
		String pre = (bSI ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
				+ (bSI ? "" : "i");
		return String.format("%.2f %sB", lbytes / Math.pow(unit, exp), pre);
	}

	// This method is called when the computation is over
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// Hide the progress dialog
			if (mProgressDialog != null)
				mProgressDialog.dismiss();
			if(null != mSelectedFileUri) {
				File fileToHash = new File(mSelectedFileUri.getPath());
				if (fileToHash != null) {
					Resources res = getResources();
					String fileName = "";
					Cursor cursor = getContentResolver().query(mSelectedFileUri, null, null, null, null);
					if (cursor != null && cursor.moveToFirst()) {
						fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
					}
					String sFileNameTitle = String
							.format(res.getString(R.string.FileName),
									fileName);
					String sFileSizeTitle = String.format(
							res.getString(R.string.FileSize), msFileSize);
					String sFileHashTitle = "";
					if (!msHash.equals("")) {
						if (mCheckBox != null) {
							if (mCheckBox.isChecked()) {
								msHash = msHash.toUpperCase();
							} else {
								msHash = msHash.toLowerCase();
							}
						}
						String Function = "";
						if (miItePos >= 0)
							Function = mFunctions[miItePos];
						sFileHashTitle = String.format(
								res.getString(R.string.Hash), Function, msHash);
						// Show the copy button
						if (mCopyButton != null)
							mCopyButton.setVisibility(View.VISIBLE);
					} else {
						sFileHashTitle = String.format(
								res.getString(R.string.unable_to_calculate),
								fileToHash.getName());
						// Hide the copy button
						if (mCopyButton != null)
							mCopyButton.setVisibility(View.INVISIBLE);
					}

					if (mResultTV != null)
						mResultTV.setText(sFileNameTitle + sFileSizeTitle + sFileHashTitle);
				}
			}
		}
	};
}