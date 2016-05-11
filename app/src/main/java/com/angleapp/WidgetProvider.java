package com.angleapp;

/**
 * Created by unnikrishnanpatel on 12/05/16.
 */
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;

public class WidgetProvider extends AppWidgetProvider {
    AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
    DynamoDBMapper dbMapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
    PaginatedQueryList<Post> result1;
    PaginatedScanList<Post> result2;
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);


        for (int widgetId : appWidgetIds) {
            final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main_layout);
            Post postToFind = new Post();
            postToFind.setUserId(Application.userId);
            AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
            final DynamoDBMapper dbMapper = awsMobileClient.getDynamoDBMapper();

            final DynamoDBQueryExpression<Post> queryExpression = new DynamoDBQueryExpression<Post>()
                    .withHashKeyValues(postToFind)
                    .withConsistentRead(false)
                    .withLimit(20);
            AsyncTask<Void,Void,PaginatedQueryList<Post>> asyncTask = new AsyncTask<Void, Void, PaginatedQueryList<Post>>() {
                @Override
                protected void onPostExecute(PaginatedQueryList<Post> result) {
                    super.onPostExecute(result);
                    remoteViews.setTextViewText(R.id.karma,String.valueOf(result1.size())+" / "+String.valueOf(result2.size()));

                }

                @Override
                protected PaginatedQueryList<Post> doInBackground(Void... params) {
                    DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                    result1 = dbMapper.query(Post.class, queryExpression);
                    result2 = dbMapper.scan(Post.class,scanExpression);

                    return result1;
                }
            };
            asyncTask.execute();



            Intent launchIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.main_widget_layout, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);

        }
    }
}