/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.map.layer.renderer;

import java.io.File;
import java.util.logging.Logger;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.Tile;
import org.mapsforge.map.layer.TileLayer;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.map.reader.header.FileOpenResult;
import org.mapsforge.map.rendertheme.XmlRenderTheme;

public class TileRendererLayer extends TileLayer<RendererJob> {

	private final MapDatabase mapDatabase;
	private final DatabaseRenderer databaseRenderer;
	private File mapFile;
	private final MapWorker mapWorker;
	private float textScale;
	private XmlRenderTheme xmlRenderTheme;

	public TileRendererLayer(TileCache tileCache, MapViewPosition mapViewPosition, GraphicFactory graphicFactory) {
		super(tileCache, mapViewPosition, graphicFactory);

		this.mapDatabase = new MapDatabase();
		this.databaseRenderer = new DatabaseRenderer(this.mapDatabase, graphicFactory);

		this.mapWorker = new MapWorker(tileCache, this.jobQueue, databaseRenderer, this);
		this.mapWorker.start();

		this.textScale = 1;
	}

	public File getMapFile() {
		return this.mapFile;
	}

	public float getTextScale() {
		return this.textScale;
	}

	public XmlRenderTheme getXmlRenderTheme() {
		return this.xmlRenderTheme;
	}

	public void setMapFile(File mapFile) {
		this.mapFile = mapFile;
		FileOpenResult result = this.mapDatabase.openFile(mapFile);
		if (!result.isSuccess()) {
			throw new IllegalArgumentException(result.getErrorMessage());
		}
	}

	public void setTextScale(float textScale) {
		this.textScale = textScale;
	}

	public void setXmlRenderTheme(XmlRenderTheme xmlRenderTheme) {
		this.xmlRenderTheme = xmlRenderTheme;
	}

	@Override
	protected RendererJob createJob(Tile tile) {
		return new RendererJob(tile, this.mapFile, this.xmlRenderTheme, this.textScale);
	}

	@Override
	protected void onAdd() {
		this.mapWorker.proceed();
		super.onAdd();
	}

	@Override
	public void onDestroy() {
		new DestroyThread(this.mapWorker, this.mapDatabase, this.databaseRenderer).start();
		super.onDestroy();
	}

	@Override
	protected void onRemove() {
		this.mapWorker.pause();
		super.onRemove();
	}
}
