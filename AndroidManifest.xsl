<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:android="http://schemas.android.com/apk/res/android">
	<xsl:param name="package" />
	<xsl:param name="title" />
	<xsl:param name="activity" />
	<xsl:param name="version" />
	<xsl:param name="versionCode" />

	<xsl:param name="minSdkVersion">8</xsl:param>
	<xsl:param name="targetSdkVersion">14</xsl:param>

	<xsl:param name="gameHash">0.0</xsl:param>
	<xsl:param name="sdkHash">1.0</xsl:param>
	<xsl:param name="androidHash">1.0</xsl:param>
	<xsl:param name="develop">false</xsl:param>
	<xsl:param name="appid"></xsl:param>
	<xsl:param name="fullscreen">true</xsl:param>
	<xsl:param name="shortname">tealeaf</xsl:param>
	<xsl:param name="studioName">Wee Cat Studios</xsl:param>
	<xsl:param name="codeHost">s.wee.cat</xsl:param>
	<xsl:param name="tcpHost">s.wee.cat</xsl:param>
	<xsl:param name="codePort">80</xsl:param>
	<xsl:param name="tcpPort">4747</xsl:param>
	<xsl:param name="entryPoint">gc.native.launchClient</xsl:param>
	<xsl:param name="pushUrl">http://staging.api.gameclosure.com/push/%s/?device=%s&amp;version=%s</xsl:param>
	<xsl:param name="servicesUrl">http://api.gameclosure.com</xsl:param>
	<xsl:param name="disableLogs">true</xsl:param>
	<xsl:param name="debuggable">false</xsl:param>
	<xsl:param name="installShortcut">false</xsl:param>

	<xsl:param name="contactsUrl"></xsl:param>

	<xsl:param name="orientation"></xsl:param>
	<xsl:strip-space elements="*" />
	<xsl:output indent="yes" />
	<xsl:template match="comment()" />

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="uses-sdk/@android:minSdkVersion">
		<xsl:attribute name="android:minSdkVersion"><xsl:value-of select="$minSdkVersion" /></xsl:attribute>
	</xsl:template>

	<xsl:template match="uses-sdk/@android:targetSdkVersion">
		<xsl:attribute name="android:targetSdkVersion"><xsl:value-of select="$targetSdkVersion" /></xsl:attribute>
	</xsl:template>

	<xsl:template match="application/@android:debuggable">
		<xsl:attribute name="android:debuggable"><xsl:value-of select="$debuggable" /></xsl:attribute>
	</xsl:template>

	<xsl:template match="manifest/@package">
		<xsl:attribute name="package"><xsl:value-of select="$package" /></xsl:attribute>
	</xsl:template>
	<xsl:template match="intent-filter/data/@android:host">
		<xsl:attribute name="android:host"><xsl:value-of select="$package" /></xsl:attribute>
	</xsl:template>
	<xsl:template match="manifest/@android:versionName">
		<xsl:attribute name="android:versionName"><xsl:value-of select="$version" /></xsl:attribute>
	</xsl:template>

	<xsl:template match="manifest/@android:versionCode">
		<xsl:attribute name="android:versionCode"><xsl:value-of select="$versionCode" /></xsl:attribute>
	</xsl:template>

	<xsl:template match="category/@android:name[.='com.tealeaf']">
		<xsl:attribute name="android:name">
			<xsl:value-of select="$package" />
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="activity/@android:label[.='TeaLeaf']">
		<xsl:attribute name="android:label"><xsl:value-of select="$title" /></xsl:attribute>
	</xsl:template>

	<xsl:template match="activity/@android:screenOrientation">
		<xsl:attribute name="android:screenOrientation"><xsl:value-of select="$orientation" /></xsl:attribute>
	</xsl:template>

	<xsl:template match="activity/@android:name[.='.TeaLeaf']">
		<xsl:attribute name="android:name"><xsl:value-of select="$activity" /></xsl:attribute>
	</xsl:template>

	<xsl:template match="meta-data[@android:name='gameHash']">
		<meta-data android:name="gameHash" android:value="{$gameHash}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='sdkHash']">
		<meta-data android:name="sdkHash" android:value="{$sdkHash}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='androidHash']">
		<meta-data android:name="androidHash" android:value="{$androidHash}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='appID']">
		<meta-data android:name="appID" android:value="{$appid}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='fullscreen']">
		<meta-data android:name="fullscreen" android:value="{$fullscreen}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='shortName']">
		<meta-data android:name="shortName" android:value="{$shortname}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='servicesUrl']">
		<meta-data android:name="servicesUrl" android:value="{$servicesUrl}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='studioName']">
		<meta-data android:name="studioName" android:value="{$studioName}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='codeHost']">
		<meta-data android:name="codeHost" android:value="{$codeHost}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='tcpHost']">
		<meta-data android:name="tcpHost" android:value="{$tcpHost}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='pushUrl']">
		<meta-data android:name="pushUrl" android:value="{$pushUrl}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='contactsUrl']">
		<meta-data android:name="contactsUrl" android:value="{$contactsUrl}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='codePort']">
		<meta-data android:name="codePort" android:value="{$codePort}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='tcpPort']">
		<meta-data android:name="tcpPort" android:value="{$tcpPort}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='entryPoint']">
		<meta-data android:name="entryPoint" android:value="{$entryPoint}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='disableLogs']">
		<meta-data android:name="disableLogs" android:value="${disableLogs}"/>
	</xsl:template>
	<xsl:template match="meta-data[@android:name='installShortcut']">
		<meta-data android:name="installShortcut" android:value="{$installShortcut}"/>
	</xsl:template>
</xsl:stylesheet>
