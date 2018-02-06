<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:android="http://schemas.android.com/apk/res/android">

  <xsl:param name="freshchatAppID" />
  <xsl:param name="freshchatAppKey" />
  <xsl:param name="freshchatTag" />
  <xsl:param name="package" />

  <xsl:output indent="yes" />
  <xsl:template match="comment()" />

  <xsl:template match="meta-data[@android:name='FRESHCHAT_APP_ID']">
    <meta-data android:name="FRESHCHAT_APP_ID" android:value="{$freshchatAppID}" />
  </xsl:template>

  <xsl:template match="meta-data[@android:name='FRESHCHAT_APP_KEY']">
    <meta-data android:name="FRESHCHAT_APP_KEY" android:value="{$freshchatAppKey}" />
  </xsl:template>

  <xsl:template match="meta-data[@android:name='FRESHCHAT_TAG']">
    <meta-data android:name="FRESHCHAT_TAG" android:value="{$freshchatTag}" />
  </xsl:template>


  <xsl:template match="provider/@android:authorities[.='your_package_name.provider']">
    <xsl:attribute name="android:authorities">
      <xsl:value-of select="concat($package, '.provider')" />
    </xsl:attribute>
  </xsl:template>

  <xsl:output indent="yes" />
  <xsl:template match="comment()" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
