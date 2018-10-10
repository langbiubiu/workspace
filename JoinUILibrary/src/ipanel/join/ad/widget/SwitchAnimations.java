package ipanel.join.ad.widget;

import java.util.Random;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ViewSwitcher;

public class SwitchAnimations {
	static Random mRandom = new Random();

	public static int mDuration = 500;

	public static void setRandomAnimation(ViewSwitcher vs) {
		Context context = vs.getContext();
		AnimationType type = AnimationType.values()[mRandom.nextInt(AnimationType.values().length)];
		vs.setInAnimation(getInAnimation(context, type));
		vs.setOutAnimation(getOutAnimation(context, type));
	}

	public enum AnimationType {
		fade_in_out, left_in_right_out, right_in_left_out, up_in_down_out, down_in_up_out, scale_in_out
	}

	public static Animation getInAnimation(Context ctx, AnimationType type) {
		Animation animation = null;
		switch (type) {
		case down_in_up_out:
			animation = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
					Animation.RELATIVE_TO_SELF, 1f, Animation.ABSOLUTE, 0);
			break;
		case fade_in_out:
			animation = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_in);
			break;
		case left_in_right_out:
			animation = AnimationUtils.loadAnimation(ctx, android.R.anim.slide_in_left);
			break;
		case right_in_left_out:
			animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1f, Animation.ABSOLUTE,
					0, Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, 0);
			break;
		case scale_in_out:
			animation = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			break;
		case up_in_down_out:
			animation = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
					Animation.RELATIVE_TO_SELF, -1f, Animation.ABSOLUTE, 0);

			break;
		default:
			break;

		}
		if (animation != null) {
			animation.setDuration(mDuration);
		}
		return animation;
	}

	public static Animation getOutAnimation(Context ctx, AnimationType type) {
		Animation animation = null;
		switch (type) {
		case down_in_up_out:
			animation = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
					Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1f);
			break;
		case fade_in_out:
			animation = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_out);
			break;
		case left_in_right_out:
			animation = AnimationUtils.loadAnimation(ctx, android.R.anim.slide_out_right);
			break;
		case right_in_left_out:
			animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
					Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0,
					Animation.ABSOLUTE, 0);
			break;
		case scale_in_out:
			animation = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			break;
		case up_in_down_out:
			animation = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
					Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);

			break;
		default:
			break;

		}
		if (animation != null) {
			animation.setDuration(mDuration);
		}
		return animation;
	}

}
