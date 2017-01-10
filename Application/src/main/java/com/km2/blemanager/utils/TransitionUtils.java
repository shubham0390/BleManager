
package com.km2.blemanager.utils;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.List;

/**
 * Utility methods for working with transitions
 */
public class TransitionUtils {

    private TransitionUtils() {
    }

    private static List<Boolean> setAncestralClipping(
            @NonNull View view, boolean clipChildren, List<Boolean> was) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            was.add(group.getClipChildren());
            group.setClipChildren(clipChildren);
        }
        ViewParent parent = view.getParent();
        if (parent != null && parent instanceof ViewGroup) {
            setAncestralClipping((ViewGroup) parent, clipChildren, was);
        }
        return was;
    }

    public static void restoreAncestralClipping(@NonNull View view, List<Boolean> was) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            group.setClipChildren(was.remove(0));
        }
        ViewParent parent = view.getParent();
        if (parent != null && parent instanceof ViewGroup) {
            restoreAncestralClipping((ViewGroup) parent, was);
        }
    }

    public static class TransitionListenerAdapter implements android.support.transition.Transition.TransitionListener {


        @Override
        public void onTransitionStart(@NonNull android.support.transition.Transition transition) {

        }

        @Override
        public void onTransitionEnd(@NonNull android.support.transition.Transition transition) {

        }

        @Override
        public void onTransitionCancel(@NonNull android.support.transition.Transition transition) {

        }

        @Override
        public void onTransitionPause(@NonNull android.support.transition.Transition transition) {

        }

        @Override
        public void onTransitionResume(@NonNull android.support.transition.Transition transition) {

        }
    }
}
