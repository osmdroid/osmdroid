// Created by plusminus on 22:59:38 - 12.09.2008
package org.osmdroid.views.overlay;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.osmdroid.library.R;

public class ItemizedOverlayControlView extends LinearLayout {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected ImageButton mPreviousButton;
	protected ImageButton mNextButton;
	protected ImageButton mCenterToButton;
	protected ImageButton mNavToButton;

	protected ItemizedOverlayControlViewListener mLis;

	// ===========================================================
	// Constructors
	// ===========================================================

	public ItemizedOverlayControlView(final Context context,
			final AttributeSet attrs) {
		super(context, attrs);

		this.mPreviousButton = new ImageButton(context);
		this.mPreviousButton
				.setImageDrawable(context.getResources().getDrawable(R.drawable.previous));

		this.mNextButton = new ImageButton(context);
		this.mNextButton.setImageDrawable(context.getResources().getDrawable(R.drawable.next));

		this.mCenterToButton = new ImageButton(context);
		this.mCenterToButton.setImageDrawable(context.getResources().getDrawable(R.drawable.center));

		this.mNavToButton = new ImageButton(context);
		this.mNavToButton
				.setImageDrawable(context.getResources().getDrawable(R.drawable.navto_small));

		this.addView(mPreviousButton, new LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		this.addView(mCenterToButton, new LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		this.addView(mNavToButton, new LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		this.addView(mNextButton, new LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

		initViewListeners();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setItemizedOverlayControlViewListener(final ItemizedOverlayControlViewListener lis) {
		this.mLis = lis;
	}

	public void setNextEnabled(final boolean pEnabled) {
		this.mNextButton.setEnabled(pEnabled);
	}

	public void setPreviousEnabled(final boolean pEnabled) {
		this.mPreviousButton.setEnabled(pEnabled);
	}

	public void setNavToVisible(final int pVisibility) {
		this.mNavToButton.setVisibility(pVisibility);
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	private void initViewListeners() {
		this.mNextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (ItemizedOverlayControlView.this.mLis != null)
					ItemizedOverlayControlView.this.mLis.onNext();
			}
		});

		this.mPreviousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (ItemizedOverlayControlView.this.mLis != null)
					ItemizedOverlayControlView.this.mLis.onPrevious();
			}
		});

		this.mCenterToButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (ItemizedOverlayControlView.this.mLis != null)
					ItemizedOverlayControlView.this.mLis.onCenter();
			}
		});

		this.mNavToButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (ItemizedOverlayControlView.this.mLis != null)
					ItemizedOverlayControlView.this.mLis.onNavTo();
			}
		});
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public interface ItemizedOverlayControlViewListener {
		public void onPrevious();

		public void onNext();

		public void onCenter();

		public void onNavTo();
	}
}
