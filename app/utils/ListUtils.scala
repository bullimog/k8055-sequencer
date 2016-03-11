package utils

import scala.concurrent.Future


object ListUtils {

  def listOfFutures2FutureList[T](lisOfFutures: List[Future[T]]): Future[List[T]] =
    lisOfFutures.foldRight(Future(Nil:List[T]))((listSoFar, currentListItem) =>
      for{
        resultList <- listSoFar
        itemToAdd <- currentListItem
      } yield resultList::itemToAdd)

}
