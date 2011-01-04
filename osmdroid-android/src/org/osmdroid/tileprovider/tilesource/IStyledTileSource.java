package org.osmdroid.tileprovider.tilesource;

public interface IStyledTileSource<T> {

	public void setStyle(T style);
	public void setStyle(String style);
	public T getStyle();
}
