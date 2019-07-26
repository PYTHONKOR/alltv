/*
 * MIT License
 *
 * Copyright (c) 2019 PYTHONKOR

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.ksi.alltv;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class VersionChecker {

    private Context mContext;
    private String mPackageName, mUpdateJsonUrl, mCurrentVersion, mPatchedVersion;
    private Dialog mDialog;
    private String mOkButtonText;
    private String mDialogMessageText;
    private String mDialogTitleText;

    public VersionChecker(Context activity) {
        this.mContext = activity;
        this.getCurrentVersion();
    }

    public void check() {
        new GetLatestVersion().execute();
    }

    public void setokButtonText(String okButtonText) {
        this.mOkButtonText = okButtonText;
    }

    public void setmDialogMessageText(String mDialogMessageText) {
        this.mDialogMessageText = mDialogMessageText;
    }

    public void setmDialogTitleText(String mDialogTitleText) {
        this.mDialogTitleText = mDialogTitleText;
    }

    public void setcheckJsonUrl(String checkUrl) {
        this.mUpdateJsonUrl = checkUrl;
    }

    public final String getAppDataString(int resourceId) {
        return mContext.getResources().getString(resourceId);
    }

    private void getCurrentVersion() {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pInfo;

        try {
            mPackageName = mContext.getPackageName();
            pInfo = pm.getPackageInfo(mContext.getPackageName(), 0);
            mCurrentVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    private class GetLatestVersion extends AsyncTask<String, String, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {

                String resultJson = HttpRequest.get(mUpdateJsonUrl, true).body();

                JsonParser jParser = new JsonParser();
                JsonObject jObj = jParser.parse(resultJson).getAsJsonObject();

                mPatchedVersion = jObj.get(getAppDataString(R.string.PATCHED_STR)).getAsString();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer jsonObject) {
            if (mPatchedVersion != null && mPatchedVersion.length() > 0) {
                if (!mCurrentVersion.equalsIgnoreCase(mPatchedVersion)) {
                    showUpdateDialog();
                }
            }

            super.onPostExecute(jsonObject);
        }
    }

    private void showUpdateDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle(mDialogTitleText != null ?
                mDialogTitleText : getAppDataString(R.string.update_title_str));

        builder.setMessage(mDialogMessageText != null ?
                mDialogMessageText : getAppDataString(R.string.update_version_str));

        builder.setPositiveButton(mOkButtonText != null ?
                mOkButtonText : getAppDataString(R.string.ok_str), (dialog, which) -> {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                dialog.dismiss();
            }
        });

        mDialog = builder.show();
    }
}
