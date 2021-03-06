package com.angleapp;
/**
 * Created by unnikrishnanpatel on 04/05/16.
 */

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAutoGeneratedKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "angleapp-mobilehub-1491286053-Posts")

public class Post implements Serializable {
    private String _userId;
    private String _postId;
    private String _author;
    private String _category;
    private String _content;
    private double _creationDate;
    private String _keyword;
    private String _title;
    private String _userImage;
    private double _voteCount;
    private Set<String> _votes;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBRangeKey(attributeName = "postId")
    @DynamoDBAutoGeneratedKey
    @DynamoDBAttribute(attributeName = "postId")
    public String getPostId() {
        return _postId;
    }

    public void setPostId(final String _postId) {
        this._postId = _postId;
    }
    @DynamoDBAttribute(attributeName = "author")
    public String getAuthor() {
        return _author;
    }

    public void setAuthor(final String _author) {
        this._author = _author;
    }
    @DynamoDBIndexHashKey(attributeName = "category", globalSecondaryIndexNames = {"Categories","VoteIndex",})
    public String getCategory() {
        return _category;
    }

    public void setCategory(final String _category) {
        this._category = _category;
    }
    @DynamoDBAttribute(attributeName = "content")
    public String getContent() {
        return _content;
    }

    public void setContent(final String _content) {
        this._content = _content;
    }
    @DynamoDBIndexRangeKey(attributeName = "creationDate", globalSecondaryIndexNames = {"Keyword","Categories","Title",})
    public double getCreationDate() {
        return _creationDate;
    }

    public void setCreationDate(final double _creationDate) {
        this._creationDate = _creationDate;
    }
    @DynamoDBIndexHashKey(attributeName = "keyword", globalSecondaryIndexName = "Keyword")
    public String getKeyword() {
        return _keyword;
    }

    public void setKeyword(final String _keyword) {
        this._keyword = _keyword;
    }
    @DynamoDBIndexHashKey(attributeName = "title", globalSecondaryIndexName = "Title")
    public String getTitle() {
        return _title;
    }

    public void setTitle(final String _title) {
        this._title = _title;
    }
    @DynamoDBAttribute(attributeName = "userImage")
    public String getUserImage() {
        return _userImage;
    }

    public void setUserImage(final String _userImage) {
        this._userImage = _userImage;
    }
    @DynamoDBIndexRangeKey(attributeName = "voteCount", globalSecondaryIndexName = "VoteIndex")
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
