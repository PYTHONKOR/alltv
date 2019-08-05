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

import android.os.Bundle;
import android.support.v4.content.ContextCompat;


public class ErrorFragment extends android.support.v17.leanback.app.ErrorSupportFragment {
    private static final boolean TRANSLUCENT = true;

    private String getStringById(int resourceId) {
        return this.getResources().getString(resourceId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getStringById(R.string.browse_title));
    }

    void setErrorContent() {
        setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.lb_ic_sad_cloud));
        setMessage(getStringById(R.string.error_fragment_message));
        setDefaultBackground(TRANSLUCENT);

        setButtonText(getStringById(R.string.dismiss_error));
        setButtonClickListener(
                arg0 -> getFragmentManager().beginTransaction().remove(ErrorFragment.this).commit());
    }
}