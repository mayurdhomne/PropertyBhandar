package com.ams.propertybhandar.model

data class MonthlyEMI(val month: Int, val principalComponent: Double, val interestComponent: Double, val outstandingBalance: Double)