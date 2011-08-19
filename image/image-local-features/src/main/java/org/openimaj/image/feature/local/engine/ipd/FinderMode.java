package org.openimaj.image.feature.local.engine.ipd;

import org.openimaj.image.feature.local.detector.ipd.finder.CharacteristicOctaveInterestPointFinder;

import org.openimaj.image.feature.local.detector.ipd.finder.LoggingOctaveInterestPointFinder;
import org.openimaj.image.feature.local.detector.ipd.finder.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.interest.IPDSelectionMode;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;

public interface FinderMode<T extends InterestPointData>{
	public OctaveInterestPointFinder<T> finder(InterestPointDetector<T> detector, IPDSelectionMode selectionMode);
	static class Basic<T extends InterestPointData> implements FinderMode<T>{
		@Override
		public OctaveInterestPointFinder<T> finder(InterestPointDetector<T> detector,IPDSelectionMode selectionMode) {
			return new OctaveInterestPointFinder<T>(detector,selectionMode);
		}
	}
	static class Logging<T extends InterestPointData> implements FinderMode<T>{
		@Override
		public OctaveInterestPointFinder<T> finder(InterestPointDetector<T> detector,IPDSelectionMode selectionMode) {
			return new LoggingOctaveInterestPointFinder<T>(detector,selectionMode);
		}
	}
	static class Characteristic<T extends InterestPointData> implements FinderMode<T>{
		private CharacteristicOctaveInterestPointFinder<T> settings;

		public Characteristic(){
			settings = new CharacteristicOctaveInterestPointFinder<T>(null,null);
		}
		
		public Characteristic(int maxDistance, double maxRotation, double maxAxisRatio){
			settings = new CharacteristicOctaveInterestPointFinder<T>(null,null);
			settings.maxDistance = maxDistance;
			settings.maxRotation = maxRotation;
			settings.maxAxisRatio = maxAxisRatio;
		}
		
		public Characteristic(int maxDistance){
			settings = new CharacteristicOctaveInterestPointFinder<T>(null,null);
			settings.maxDistance = maxDistance;
		}
		
		@Override
		public OctaveInterestPointFinder<T> finder(InterestPointDetector<T> detector,IPDSelectionMode selectionMode) {
			CharacteristicOctaveInterestPointFinder<T> n = new CharacteristicOctaveInterestPointFinder<T>(detector,selectionMode);
			n.maxDistance = settings.maxDistance;
			n.maxRotation = settings.maxRotation;
			n.maxAxisRatio = settings.maxAxisRatio;
			return n;
		}
	}
}