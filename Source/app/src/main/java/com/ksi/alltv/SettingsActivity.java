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

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v17.leanback.app.GuidedStepSupportFragment;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v17.leanback.widget.GuidanceStylist.Guidance;
import android.support.v17.leanback.widget.GuidedDatePickerAction;
import android.support.v4.app.FragmentManager;
import android.text.InputType;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;


public class SettingsActivity extends FragmentActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static SettingsData mSettings;

    public enum GuidedId {
        GuidedIdStart, Oksusu, OksusuId, OksusuPw, OksusuQuality, OksusuAuto, OksusuFullHd, OksusuHd, OksusuSd, OksusuSave,
        Pooq, PooqId, PooqPw, PooqQuality, PooqMobile, PooqSD, PooqHD, PooqFHD, PooqSave
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {

            Gson gson = new Gson();
            mSettings = gson.fromJson(getIntent().getStringExtra(getResources().getString(R.string.SETTINGSDATA_STR)), SettingsData.class);

            GuidedStepSupportFragment.addAsRoot(this, new MainStepFragment(), android.R.id.content);
        }
    }

    private static void addAction(Context context, List<GuidedAction> actions, long id, String title, String desc) {
        actions.add(new GuidedAction.Builder(context)
                .id(id)
                .title(title)
                .description(desc)
                .build());
    }

    private static void addEditableAction(Context context, List<GuidedAction> actions, long id, String title, String desc, int inputType) {
        actions.add(new GuidedAction.Builder(context)
                .id(id)
                .title(title)
                .descriptionEditable(true)
                .descriptionInputType(inputType)
                .description(desc)
                .build());
    }

    private static void addEditablePasswordAction(Context context, List<GuidedAction> actions, long id, String title, String desc, String password) {
        actions.add(new GuidedAction.Builder(context)
                .id(id)
                .title(title)
                .description(desc)
                .editDescription(password)
                .descriptionEditInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .descriptionEditable(true)
                .build());
    }


    private static void addDropDownAction(Context context, List<GuidedAction> actions, long id, String title, String desc, List<GuidedAction> selectionActions) {
        actions.add(new GuidedAction.Builder(context)
                .id(id)
                .title(title)
                .description(desc)
                .subActions(selectionActions)
                .build());
    }

    private static void addCheckedAction(Context context, List<GuidedAction> actions, int id, String title, String desc, boolean checked) {
        GuidedAction guidedAction = new GuidedAction.Builder(context)
                .title(title)
                .description(desc)
                .checkSetId(id)
                .build();
        guidedAction.setChecked(checked);
        actions.add(guidedAction);
    }

    private static void addDateAction(Context context, List<GuidedAction> actions, long id, String title, long date) {
        actions.add(new GuidedDatePickerAction.Builder(context)
                .id(id)
                .date(date)
                .datePickerFormat("YMD")
                .maxDate(new Date().getTime())
                .title(title)
                .build());
    }

    // MainStepFragment
    public static class MainStepFragment extends GuidedStepSupportFragment {
        @Override
        @NonNull
        public Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
            String title = getResources().getString(R.string.settings_title);
            String breadcrumb = getResources().getString(R.string.browse_title);
            String description = getResources().getString(R.string.settings_desc);
            Drawable icon = getActivity().getDrawable(R.drawable.settings_icon);

            return new Guidance(title, description, breadcrumb, icon);
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions,
                                    Bundle savedInstanceState) {

            addAction(getContext(), actions, GuidedId.Oksusu.ordinal(),
                    getResources().getString(R.string.oksususettings),
                    getResources().getString(R.string.setdesc));

            addAction(getContext(), actions, GuidedId.Pooq.ordinal(),
                    getResources().getString(R.string.pooqsettings),
                    getResources().getString(R.string.setdesc));
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            FragmentManager fm = getFragmentManager();

            if (action.getId() == GuidedId.Oksusu.ordinal()) {
                GuidedStepSupportFragment.add(fm, new OksusuStepFragment());
            } else if (action.getId() == GuidedId.Pooq.ordinal()) {
                GuidedStepSupportFragment.add(fm, new PooqStepFragment());
            }
        }
    }

    // OksusuStepFragment
    public static class OksusuStepFragment extends GuidedStepSupportFragment {
        @Override
        @NonNull
        public Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
            String title = getResources().getString(R.string.oksususettings);
            String breadcrumb = getResources().getString(R.string.browse_title);
            String description = getResources().getString(R.string.setdesc);
            Drawable icon = getActivity().getDrawable(R.drawable.oksusu_icon);

            return new Guidance(title, description, breadcrumb, icon);
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions,
                                    Bundle savedInstanceState) {

            SettingsData.OksusuSettingsData oksusuSettings = mSettings.mOksusuSettings;

            String oksusuId = oksusuSettings.mId == null ? "" : oksusuSettings.mId;
            String oksusuPw = oksusuSettings.mPassword == null ? "" : oksusuSettings.mPassword;

            addEditableAction(getContext(), actions, GuidedId.OksusuId.ordinal(),
                    getResources().getString(R.string.id), oksusuId, InputType.TYPE_CLASS_TEXT);

            addEditablePasswordAction(getContext(), actions, GuidedId.OksusuPw.ordinal(),
                    getResources().getString(R.string.password), "", oksusuPw);

            ArrayList<GuidedAction> qualityList = new ArrayList<>();

            qualityList.add(new GuidedAction.Builder(getContext()).
                    title(getResources().getString(R.string.auto)).id(GuidedId.OksusuAuto.ordinal()).build());
            qualityList.add(new GuidedAction.Builder(getContext()).
                    title(getResources().getString(R.string.fullhd)).id(GuidedId.OksusuFullHd.ordinal()).build());
            qualityList.add(new GuidedAction.Builder(getContext()).
                    title(getResources().getString(R.string.hd)).id(GuidedId.OksusuHd.ordinal()).build());
            qualityList.add(new GuidedAction.Builder(getContext()).
                    title(getResources().getString(R.string.sd)).id(GuidedId.OksusuSd.ordinal()).build());

            addDropDownAction(getContext(), actions, GuidedId.OksusuQuality.ordinal(),
                    getResources().getString(R.string.quality_set), "", qualityList);
        }

        @Override
        public void onCreateButtonActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            super.onCreateButtonActions(actions, savedInstanceState);

            addAction(getContext(), actions, GuidedId.OksusuSave.ordinal(), getResources().getString(R.string.set_save), "");
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {

            if (action.getId() == GuidedId.OksusuSave.ordinal()) {

                String id = findActionById(GuidedId.OksusuId.ordinal()).getDescription().toString();
                String password = findActionById(GuidedId.OksusuPw.ordinal()).getEditDescription().toString();

                if (id == null || id.length() == 0 || password == null || password.length() == 0) {
                    Utils.showToast(getContext(), R.string.input_error);
                    return;
                }

                mSettings.mOksusuSettings.mId = id;
                mSettings.mOksusuSettings.mPassword = password;

                if (mSettings.mOksusuSettings.mQualityType == null)
                    mSettings.mOksusuSettings.mQualityType = SettingsData.OksusuQualityType.AUTO;

                Intent intent = new Intent();

                Gson gson = new Gson();
                String myJson = gson.toJson(mSettings);
                intent.putExtra(getResources().getString(R.string.SETTINGSDATA_STR), myJson);

                getActivity().setResult(Utils.Code.OksusuSave.ordinal(), intent);
                getActivity().finishAfterTransition();
            }
        }

        @Override
        public boolean onSubGuidedActionClicked(GuidedAction action) {

            if (action.getId() == GuidedId.OksusuAuto.ordinal()) {
                mSettings.mOksusuSettings.mQualityType = SettingsData.OksusuQualityType.AUTO;
            } else if (action.getId() == GuidedId.OksusuFullHd.ordinal()) {
                mSettings.mOksusuSettings.mQualityType = SettingsData.OksusuQualityType.FullHD;
            } else if (action.getId() == GuidedId.OksusuHd.ordinal()) {
                mSettings.mOksusuSettings.mQualityType = SettingsData.OksusuQualityType.HD;
            } else if (action.getId() == GuidedId.OksusuSd.ordinal()) {
                mSettings.mOksusuSettings.mQualityType = SettingsData.OksusuQualityType.SD;
            }

            Utils.showToast(getContext(), mSettings.mOksusuSettings.mQualityType.toString() + " " + getResources().getString(R.string.select));

            return true;
        }
    }

    // PooqStepFragment
    public static class PooqStepFragment extends GuidedStepSupportFragment {
        @Override
        @NonNull
        public Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
            String title = getResources().getString(R.string.pooqsettings);
            String breadcrumb = getResources().getString(R.string.browse_title);
            String description = getResources().getString(R.string.setdesc);
            Drawable icon = getActivity().getDrawable(R.drawable.pooq_icon);

            return new Guidance(title, description, breadcrumb, icon);
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions,
                                    Bundle savedInstanceState) {

            SettingsData.PooqSettingsData pooqSettings = mSettings.mPooqSettings;

            String id = pooqSettings.mId == null ? "" : pooqSettings.mId;
            String pw = pooqSettings.mPassword == null ? "" : pooqSettings.mPassword;

            addEditableAction(getContext(), actions, GuidedId.PooqId.ordinal(),
                    getResources().getString(R.string.id), id, InputType.TYPE_CLASS_TEXT);

            addEditablePasswordAction(getContext(), actions, GuidedId.PooqPw.ordinal(),
                    getResources().getString(R.string.password), "", pw);

            ArrayList<GuidedAction> qualityList = new ArrayList<>();

            qualityList.add(new GuidedAction.Builder(getContext()).
                    title(getResources().getString(R.string.mobile)).id(GuidedId.PooqMobile.ordinal()).build());
            qualityList.add(new GuidedAction.Builder(getContext()).
                    title(getResources().getString(R.string.sd)).id(GuidedId.PooqSD.ordinal()).build());
            qualityList.add(new GuidedAction.Builder(getContext()).
                    title(getResources().getString(R.string.hd)).id(GuidedId.PooqHD.ordinal()).build());
            qualityList.add(new GuidedAction.Builder(getContext()).
                    title(getResources().getString(R.string.fullhd)).id(GuidedId.PooqFHD.ordinal()).build());

            addDropDownAction(getContext(), actions, GuidedId.PooqQuality.ordinal(),
                    getResources().getString(R.string.quality_set), "", qualityList);
        }

        @Override
        public void onCreateButtonActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            super.onCreateButtonActions(actions, savedInstanceState);

            addAction(getContext(), actions, GuidedId.PooqSave.ordinal(), getResources().getString(R.string.set_save), "");
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {

            if (action.getId() == GuidedId.PooqSave.ordinal()) {

                String id = findActionById(GuidedId.PooqId.ordinal()).getDescription().toString();
                String password = findActionById(GuidedId.PooqPw.ordinal()).getEditDescription().toString();

                if (id == null || id.length() == 0 || password == null || password.length() == 0) {
                    Utils.showToast(getContext(), R.string.input_error);
                    return;
                }

                mSettings.mPooqSettings.mId = id;
                mSettings.mPooqSettings.mPassword = password;

                if (mSettings.mPooqSettings.mQualityType == null)
                    mSettings.mPooqSettings.mQualityType = SettingsData.PooqQualityType.Mobile;

                Intent intent = new Intent();

                Gson gson = new Gson();
                String myJson = gson.toJson(mSettings);
                intent.putExtra(getResources().getString(R.string.SETTINGSDATA_STR), myJson);

                getActivity().setResult(Utils.Code.PooqSave.ordinal(), intent);
                getActivity().finishAfterTransition();
            }
        }

        @Override
        public boolean onSubGuidedActionClicked(GuidedAction action) {

            if (action.getId() == GuidedId.PooqMobile.ordinal()) {
                mSettings.mPooqSettings.mQualityType = SettingsData.PooqQualityType.Mobile;
            } else if (action.getId() == GuidedId.PooqSD.ordinal()) {
                mSettings.mPooqSettings.mQualityType = SettingsData.PooqQualityType.SD;
            } else if (action.getId() == GuidedId.PooqHD.ordinal()) {
                mSettings.mPooqSettings.mQualityType = SettingsData.PooqQualityType.HD;
            } else if (action.getId() == GuidedId.PooqFHD.ordinal()) {
                mSettings.mPooqSettings.mQualityType = SettingsData.PooqQualityType.FHD;
            }

            Utils.showToast(getContext(), mSettings.mPooqSettings.mQualityType.toString() + " " + getResources().getString(R.string.select));

            return true;
        }
    }
}
