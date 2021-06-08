package ru.sem.animalfeed.model

import org.threeten.bp.LocalDateTime

class HistoryQuery(var animalId:Long, var hType: HTypeWrapper, var startDate: LocalDateTime, var endDate: LocalDateTime, var allTime: Boolean)