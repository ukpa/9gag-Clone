package com.angleapp;

/**
 * Created by unnikrishnanpatel on 11/05/16.
 */
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "angleapp-mobilehub-1491286053-Comments")

public class Comment {
    private String _postId;
    private double _creationDate;
    private String _comment;
    private String _userid;
    private String _username;
    private double _voteCount;
    private Set<String> _votes;

    @DynamoDBHashKey(attributeName = "postId")
    @DynamoDBIndexHashKey(attributeName = "postId", globalSecondaryIndexName = "CommentVoteSort")
    public String getPostId() {
        return _postId;
    }

    public void setPostId(final String _postId) {
        this._postId = _postId;
    }
    @DynamoDBRangeKey(attributeName = "creationDate")
    @DynamoDBAttribute(attributeName = "creationDate")
    public double getCreationDate() {
        return _creationDate;
    }

    public void setCreationDate(final double _creationDate) {
        this._creationDate = _creationDate;
    }
    @DynamoDBAttribute(attributeName = "comment")
    public String getComment() {
        return _comment;
    }

    public void setComment(final String _comment) {
        this._comment = _comment;
    }
    @DynamoDBAttribute(attributeName = "userid")
    public String getUserid() {
        return _userid;
    }

    public void setUserid(final String _userid) {
        this._userid = _userid;
    }
    @DynamoDBAttribute(attributeName = "username")
    public String getUsername() {
        return _username;
    }

    public void setUsername(final String _username) {
        this._username = _username;
    }
    @DynamoDBIndexRangeKey(attributeName = "voteCount", globalSecondaryIndexName = "CommentVoteSort")
    public double getVoteCount() {
        return _voteCount;
    }

    public void setVoteCount(final double _voteCount) {
        this._voteCount = _voteCount;
    }
    @DynamoDBAttribute(attributeName = "votes")
    public Set<String> getVotes() {
        return _votes;
    }

    public void setVotes(final Set<String> _votes) {
        this._votes = _votes;
    }

}