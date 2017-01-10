package com.km2.blemanager.widgets;

import android.support.v7.widget.RecyclerView;


public class CommentAnimator extends SlideInItemAnimator {

    private boolean animateMoves = false;

    public CommentAnimator() {
        super();
    }

   public void setAnimateMoves(boolean animateMoves) {
        this.animateMoves = animateMoves;
    }

    @Override
    public boolean animateMove(
            RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        if (!animateMoves) {
            dispatchMoveFinished(holder);
            return false;
        }
        return super.animateMove(holder, fromX, fromY, toX, toY);
    }
}